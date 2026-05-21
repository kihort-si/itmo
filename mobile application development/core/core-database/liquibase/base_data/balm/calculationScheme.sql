INSERT INTO balm.scheme_script (scht_id, code, script, enabled)
VALUES (
    1, 'stock_purchase',
    '
def quantity = parameters.quantity as int
def price = parameters.price as BigDecimal
def subtotal = price * quantity

def commParams = [subtotal: subtotal, regionId: parameters.regionId, clientId: parameters.clientId]
def commissionResult = commissionService.calculate("stock_purchase_commission", parameters.clientId as int, commParams)
def commission = commissionResult.resultFee
def feePercent = commissionResult.feePercent

return [
    subtotal: subtotal,
    commission: commission,
    feePercent: feePercent,
    total: subtotal + commission
]
    ',
    true
);

INSERT INTO balm.scheme_script (scht_id, code, script, enabled)
VALUES (
    1, 'stock_sell',
    '
def quantity = parameters.quantity as int
def price = parameters.price as BigDecimal
def subtotal = price * quantity

def commParams = [subtotal: subtotal, regionId: parameters.regionId, clientId: parameters.clientId]
def commissionResult = commissionService.calculate("stock_sell_commission", parameters.clientId as int, commParams)
def commission = commissionResult.resultFee
def feePercent = commissionResult.feePercent

return [
    subtotal: subtotal,
    commission: commission,
    feePercent: feePercent,
    total: subtotal - commission
]
    ',
    true
);

