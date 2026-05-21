// ============================================================
// File: com/vt/balm/table/Tables.kt
// ============================================================
package com.vt.table

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb

object CurrencyCodeTable : Table("balm.currency_code") {
    val currId = integer("curr_id").autoIncrement()
    val code = varchar("code", 3).uniqueIndex()
    val refsId = integer("refs_id").uniqueIndex()

    override val primaryKey = PrimaryKey(currId)
}

object AccountStateTable : Table("balm.account_state") {
    val accstId = integer("accst_id").autoIncrement()
    val name = varchar("name", 255).uniqueIndex()

    override val primaryKey = PrimaryKey(accstId)
}

object AccountTable : Table("balm.account") {
    val accId = integer("acc_id").autoIncrement()
    val clntId = integer("clnt_id")
    val currId = reference("curr_id", CurrencyCodeTable.currId)
    val accstId = reference("accst_id", AccountStateTable.accstId)

    override val primaryKey = PrimaryKey(accId)
}

object BillDetailsTable : Table("balm.bill_details") {
    val bdetId = integer("bdet_id").autoIncrement()
    val code = varchar("code", 255).uniqueIndex()
    val bdetRefsId = integer("bdet_refs_id").uniqueIndex()

    override val primaryKey = PrimaryKey(bdetId)
}

object ChargeTypesTable : Table("balm.charge_types") {
    val chtpId = integer("chtp_id").autoIncrement()
    val code = varchar("code", 255).uniqueIndex()
    val defaultBdetId = reference("default_bdet_id", BillDetailsTable.bdetId)

    override val primaryKey = PrimaryKey(chtpId)
}

object ChargeTable : Table("balm.charge") {
    val chtgId = long("chtg_id").autoIncrement()
    val accId = reference("acc_id", AccountTable.accId)
    val chtpId = reference("chtp_id", ChargeTypesTable.chtpId)
    val amount = decimal("amount", 15, 2)
    val bdetId = reference("bdet_id", BillDetailsTable.bdetId).nullable()
    val timestamp = timestamp("timestamp").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(chtgId)
}

object BalanceCacheTable : Table("balm.balance_cache") {
    val baccId = integer("bacc_id").autoIncrement()
    val accId = reference("acc_id", AccountTable.accId).uniqueIndex()
    val balance = decimal("balance", 15, 2)

    override val primaryKey = PrimaryKey(baccId)
}

//object PurchaseOrderTypeTable : Table("balm.purchase_order_type") {
//    val potpId = integer("potp_id").autoIncrement()
//    val code = varchar("code", 255).uniqueIndex()
//
//    override val primaryKey = PrimaryKey(potpId)
//}
//
//object PurchaseOrderStatusTable : Table("balm.purchase_order_status") {
//    val postId = integer("post_id").autoIncrement()
//    val code = varchar("code", 255).uniqueIndex()
//
//    override val primaryKey = PrimaryKey(postId)
//}
//
//object PurchaseOrderTable : Table("balm.purchase_order") {
//    val pordId = integer("pord_id").autoIncrement()
//    val accId = reference("acc_id", AccountTable.accId)
//    val potpId = reference("potp_id", PurchaseOrderTypeTable.potpId)
//    val postId = reference("post_id", PurchaseOrderStatusTable.postId)
//    val dateStart = timestamp("date_start").clientDefault { java.time.Instant.now() }
//    val dateEnd = timestamp("date_end").nullable()
//    val orderData = jsonb("order_data", { it.toString() }, { it }) // упрощённо, если нужно сложнее
//
//    override val primaryKey = PrimaryKey(pordId)
//}

object ClientAttributeTable : Table("balm.client_attribute") {
    val clntAttrId = integer("clnt_attr_id").autoIncrement()
    val clntId = integer("clnt_id")
    val attributeRefsId = integer("attribute_refs_id")
    val startDate = timestamp("start_date").clientDefault { java.time.Instant.now() }
    val endDate = timestamp("end_date").nullable()
    val value = varchar("value", 512)

    override val primaryKey = PrimaryKey(clntAttrId)

    init {
        uniqueIndex(clntId, attributeRefsId)
    }
}

object ScheduledReportTable : Table("balm.scheduled_report") {
    val schedRepId = integer("sched_rep_id").autoIncrement()
    val clntId = integer("clnt_id")
    val reportType = varchar("report_type", 255)
    val schedule = jsonb("schedule", { it.toString() }, { it }) // упрощённо как JsonNode
    val startDate = timestamp("start_date").clientDefault { java.time.Instant.now() }
    val endDate = timestamp("end_date").nullable()

    override val primaryKey = PrimaryKey(schedRepId)

    init {
        uniqueIndex(clntId, reportType)
    }
}

object SchemeTypeTable : Table("balm.scheme_type") {
    val schtId = integer("scht_id").autoIncrement()
    val code = varchar("code", 255).uniqueIndex()

    override val primaryKey = PrimaryKey(schtId)
}

object SchemeScriptTable : Table("balm.scheme_script") {
    val schemId = integer("schem_id").autoIncrement()
    val schtId = reference("scht_id", SchemeTypeTable.schtId)
    val code = varchar("code", 255).uniqueIndex()
    val script = text("script")
    val enabled = bool("enabled").default(true)

    override val primaryKey = PrimaryKey(schemId)
}

object FeesTable : Table("balm.fees") {
    val feeId = integer("fee_id").autoIncrement()
    val regionId = integer("region_id")
    val code = varchar("code", 255).uniqueIndex()
    val amount = decimal("amount", 5, 2)

    override val primaryKey = PrimaryKey(feeId)
}