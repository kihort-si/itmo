package com.vt.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.ConnectionFactory
import com.vt.table.ScheduledReportTable
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object ScheduledReportService {
    private val logger = LoggerFactory.getLogger(ScheduledReportService::class.java)
    private val mapper = jacksonObjectMapper()

    private const val LOCK_ID = 123456789L
    private var exchangeName: String = ""
    private var rabbitHost: String = ""
    private var rabbitPort: Int = 0
    private var rabbitVhost: String = ""
    private var rabbitUser: String = ""
    private var rabbitPassword: String = ""
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun init(
        exchange: String,
        host: String,
        port: Int,
        vhost: String,
        user: String,
        password: String
    ) {
        exchangeName = exchange
        rabbitHost = host
        rabbitPort = port
        rabbitVhost = vhost
        rabbitUser = user
        rabbitPassword = password
    }

    fun startScheduler() {
        scope.launch {
            while (isActive) {
                val now = ZonedDateTime.now(ZoneOffset.UTC)
                val nextHour = now.plusHours(1).truncatedTo(ChronoUnit.HOURS)
                val delayMs = ChronoUnit.MILLIS.between(now, nextHour)
                delay(delayMs)
                val currentHour = ZonedDateTime.now(ZoneOffset.UTC).hour
                processHour(currentHour)
            }
        }
    }

    private suspend fun processHour(hour: Int) {
        val lockAcquired = transaction {
            exec("SELECT pg_try_advisory_xact_lock($LOCK_ID)") { rs ->
                rs.next() && rs.getBoolean(1)
            } ?: false
        }
        if (!lockAcquired) {
            logger.info("Skipping report sending for hour $hour, another transaction holds lock")
            return
        }
        try {
            sendReportsForHour(hour)
        } catch (e: Exception) {
            logger.error("Failed to send reports for hour $hour", e)
        }
    }

    fun manualSend(hour: Int) {
        val lockAcquired = transaction {
            try {
                exec("SET LOCAL lock_timeout = '5s'")
                exec("SELECT pg_advisory_xact_lock($LOCK_ID)")
                true
            } catch (e: Exception) {
                false
            }
        }
        if (!lockAcquired) {
            throw IllegalStateException("Another process is already sending reports")
        }
        runBlocking {
            sendReportsForHour(hour)
        }
    }

    private suspend fun sendReportsForHour(hour: Int) = withContext(Dispatchers.IO) {
        logger.info("Starting report sending for hour={} (UTC)", hour)

        val reports = transaction {
            val now = Instant.now()
            val allRows = ScheduledReportTable.selectAll()
                .where {
                    ScheduledReportTable.endDate.isNull() or
                            (ScheduledReportTable.endDate greaterEq now)
                }
                .toList()  // материализуем для логирования

            logger.info("Found {} candidate report rows before hour filtering", allRows.size)

            allRows
                .map { row ->
                    val id = row[ScheduledReportTable.schedRepId]
                    val scheduleRaw = row[ScheduledReportTable.schedule]
                    val parsedHour = extractHourFromSchedule(scheduleRaw)
                    logger.debug("Row id={}, schedule='{}', parsedHour={}", id, scheduleRaw, parsedHour)
                    id to row to parsedHour
                }
                .filter { (_, parsedHour) -> parsedHour == hour }
                .also { logger.info("After hour filtering, {} rows remain", it.size) }
                .map { it.first }  // оставляем пару (id, row)
        }

        if (reports.isEmpty()) {
            logger.info("No reports to send for hour $hour")
            return@withContext
        }

        logger.info("Preparing to send {} reports", reports.size)

        val factory = ConnectionFactory().apply {
            host = rabbitHost
            port = rabbitPort
            virtualHost = rabbitVhost
            username = rabbitUser
            password = rabbitPassword
        }
        factory.newConnection().use { conn ->
            conn.createChannel().use { channel ->
                reports.forEach { (schedRepId, row) ->
                    val message = mapOf(
                        "type" to "report",
                        "report_type" to row[ScheduledReportTable.reportType],
                        "data" to mapOf(
                            "sched_rep_id" to schedRepId,
                            "clnt_id" to row[ScheduledReportTable.clntId],
                            "report_type" to row[ScheduledReportTable.reportType],
                            "schedule" to row[ScheduledReportTable.schedule],
                            "start_date" to row[ScheduledReportTable.startDate].toString(),
                            "end_date" to row[ScheduledReportTable.endDate]?.toString()
                        )
                    )
                    channel.basicPublish(exchangeName, "", null, mapper.writeValueAsBytes(message))
                    logger.debug("Sent report for sched_rep_id=$schedRepId")
                }
            }
        }

        logger.info("Successfully sent {} reports for hour {}", reports.size, hour)
    }

    private fun extractHourFromSchedule(scheduleRaw: String): Int? {
        return try {
            val node = mapper.readTree(scheduleRaw)
            node["hour"]?.asInt()
        } catch (e: Exception) {
            logger.warn("Failed to parse schedule: {}", scheduleRaw, e)
            null
        }
    }

    fun shutdown() {
        scope.cancel()
    }
}