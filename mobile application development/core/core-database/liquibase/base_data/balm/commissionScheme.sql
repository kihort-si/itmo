INSERT INTO balm.scheme_script (scht_id, code, script, enabled)
VALUES (
    2, 'stock_purchase_commission',
    '
def regionId = parameters.regionId as int
def clientId = parameters.clientId as int
def subtotal = parameters.subtotal as BigDecimal

// Определяем код комиссии по приоритету атрибутов
def commissionCode = null
if (ctx.hasActiveAttribute(clientId, 1)) {
    commissionCode = "stock_purchase_r1_commission"
} else if (ctx.hasActiveAttribute(clientId, 2)) {
    commissionCode = "stock_purchase_r2_commission"
} else if (ctx.hasActiveAttribute(clientId, 3)) {
    commissionCode = "stock_purchase_r3_commission"
} else {
    commissionCode = "stock_purchase_base_commission"
}

def fee = ctx.fee(commissionCode, regionId)
if (!fee) {
    throw new Exception("Fee not found: $commissionCode for region $regionId")
}
def commission = subtotal * fee / 100.0  // fee – это процент

return [
    resultFee: commission,
    feePercent: fee
]
    ',
    true
);

INSERT INTO balm.scheme_script (scht_id, code, script, enabled)
VALUES (
    2, 'stock_sell_commission',
    '
def regionId = parameters.regionId as int
def clientId = parameters.clientId as int
def subtotal = parameters.subtotal as BigDecimal

def commissionCode = null
if (ctx.hasActiveAttribute(clientId, 1)) {
    commissionCode = "stock_sell_r1_commission"
} else if (ctx.hasActiveAttribute(clientId, 2)) {
    commissionCode = "stock_sell_r2_commission"
} else if (ctx.hasActiveAttribute(clientId, 3)) {
    commissionCode = "stock_sell_r3_commission"
} else {
    commissionCode = "stock_sell_base_commission"
}

def fee = ctx.fee(commissionCode, regionId)
if (!fee) {
    throw new Exception("Fee not found: $commissionCode for region $regionId")
}
def commission = subtotal * fee / 100.0

return [
    resultFee: commission,
    feePercent: fee
]
    ',
    true
);