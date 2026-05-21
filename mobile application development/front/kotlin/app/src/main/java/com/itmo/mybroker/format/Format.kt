package com.itmo.mybroker.format

import com.itmo.mybroker.data.Ccy
import kotlin.math.abs

object Fmt {
    private const val NBSP = ' '

    fun num(n: Double?, dp: Int = 2): String {
        if (n == null || n.isNaN()) return "—"
        val negative = n < 0
        val abs = abs(n)
        val whole = abs.toLong()
        val frac = abs - whole
        val wholeStr = group(whole)
        return buildString {
            if (negative) append('-')
            append(wholeStr)
            if (dp > 0) {
                append('.')

                val mult = Math.pow(10.0, dp.toDouble())
                val rounded = Math.round(frac * mult).toLong().toString().padStart(dp, '0')
                append(rounded.takeLast(dp))
            }
        }
    }

    private fun group(n: Long): String {
        val s = n.toString()
        if (s.length <= 3) return s
        val out = StringBuilder()
        val first = s.length % 3
        if (first > 0) {
            out.append(s, 0, first)
            if (first < s.length) out.append(NBSP)
        }
        var i = first
        while (i < s.length) {
            out.append(s, i, i + 3)
            if (i + 3 < s.length) out.append(NBSP)
            i += 3
        }
        return out.toString()
    }

    fun money(n: Double?, ccy: Ccy, dp: Int? = null): String {
        if (n == null || n.isNaN()) return "—"
        val sym = when (ccy) { Ccy.RUB -> "₽"; Ccy.USD -> "$"; Ccy.EUR -> "€" }
        val decimals = dp ?: 2
        val s = num(n, decimals)
        return if (ccy == Ccy.RUB) "$s $sym" else "$sym$s"
    }

    fun pct(n: Double?, dp: Int = 2, withSign: Boolean = true): String {
        if (n == null || n.isNaN()) return "—"
        val rounded = "%.${dp}f".format(n)
        val sign = if (withSign && n > 0) "+" else ""
        return "$sign$rounded%"
    }

    fun signed(n: Double?, dp: Int = 2): String {
        if (n == null || n.isNaN()) return "—"
        return (if (n > 0) "+" else "") + num(n, dp)
    }

    fun vol(n: Double?): String {
        if (n == null) return "—"
        return when {
            n >= 1e9 -> "%.2fB".format(n / 1e9)
            n >= 1e6 -> "%.2fM".format(n / 1e6)
            n >= 1e3 -> "%.1fK".format(n / 1e3)
            else -> n.toLong().toString()
        }
    }

    fun ccySym(c: Ccy): String = when (c) { Ccy.RUB -> "₽"; Ccy.USD -> "$"; Ccy.EUR -> "€" }
}
