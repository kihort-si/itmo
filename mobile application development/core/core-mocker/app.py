from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/api/v1/data_collector/getLastCurrencyExhangeCourse', methods=['GET'])
def get_currency_rate():
    # Получаем параметры из запроса
    currency_from = request.args.get('currencyFrom', 'UNKNOWN')
    currency_to = request.args.get('currencyTo', 'UNKNOWN')
    
    # Формируем мок-ответ
    response = {
        "status": "success",
        "data": {
            "currencyFrom": currency_from.upper(),
            "currencyTo": currency_to.upper(),
            "rate": 0.011,  # Фейковый курс
            "timestamp": "2023-10-27T12:00:00Z"
        }
    }
    
    return jsonify(response)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8030)