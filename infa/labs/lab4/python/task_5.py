from xml.etree import ElementTree
import csv

xml = ElementTree.parse("files/income.xml")

csvfile = open("files/outcome_task_5.csv", 'w', encoding='utf-8', newline='')
csvfile_writer = csv.writer(csvfile)

csvfile_writer.writerow(["предмет", "время_начала", "время_окончания", "вид_занятия", "преподаватель", "аудитория"])

for pairs in xml.findall(".//пара"):

    if (pairs):
        name = pairs.find(".//предмет")
        time_start = pairs.find(".//время_начала")
        time_end = pairs.find(".//время_окончания")
        class_type = pairs.find(".//вид_занятия")
        teacher = pairs.find(".//преподаватель")
        room = pairs.find(".//аудитория")

        name_text = name.text if name is not None else ''
        time_start_text = time_start.text if time_start is not None else ''
        time_end_text = time_end.text if time_end is not None else ''
        class_type_text = class_type.text if class_type is not None else ''
        teacher_text = teacher.text if teacher is not None else ''
        room_text = room.text if room is not None else ''

        csv_line = [name_text, time_start_text, time_end_text, class_type_text, teacher_text, room_text]

        csvfile_writer.writerow(csv_line)

csvfile.close()
