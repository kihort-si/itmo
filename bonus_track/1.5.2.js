db.student.countDocuments({ "name": "Anna" })
db.student.find({ "surname": "Ivanov" }).sort({ "age": 1 }).limit(1)
db.UNDERGROUND.countDocuments({ "Line": { $regex: "Bakerloo" } })
