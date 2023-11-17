import xmlplain

read_file = 'files/income.xml'
write_file = 'files/outcome_task_1.yaml'

with open(read_file, 'r', encoding='utf-8') as xml, open(write_file, "r+", encoding='utf-8') as yaml:
    root = xmlplain.xml_to_obj(xml, strip_space=True, fold_dict=True)
    xmlplain.obj_to_yaml(root, yaml)