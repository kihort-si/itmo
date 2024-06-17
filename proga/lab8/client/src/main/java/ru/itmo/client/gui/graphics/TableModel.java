package ru.itmo.client.gui.graphics;

import ru.itmo.common.collection.Person;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TableModel extends AbstractTableModel {
    private List<Person> dataList;
    private final String[] columnNames;
    private List<Person> filteredData;

    public TableModel(List<Person> dataList, String[] columnNames) {
        this.dataList = new ArrayList<>(dataList);
        this.filteredData = new ArrayList<>(dataList);
        this.columnNames = columnNames;
    }
    @Override
    public int getRowCount() {
        return filteredData.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Person person = filteredData.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> person.getId();
            case 1 -> person.getName();
            case 2 -> person.getCoordinates().getX();
            case 3 -> person.getCoordinates().getY();
            case 4 -> person.getCreationDate();
            case 5 -> person.getHeight();
            case 6 -> person.getWeight();
            case 7 -> person.getEyeColor();
            case 8 -> person.getNationality();
            case 9 -> person.getLocation().getX();
            case 10 -> person.getLocation().getY();
            case 11 -> person.getLocation().getZ();
            case 12 -> person.getCreator();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void updateData(List<Person> dataList) {
        this.dataList = new ArrayList<>(dataList);
        fireTableDataChanged();
    }

    public void sort(int columnIndex, boolean ascending) {
        Comparator<Person> comparator;

        switch (columnIndex) {
            case 0: // id
                comparator = Comparator.comparingLong(Person::getId);
                break;
            case 1: // name
                comparator = Comparator.comparing(Person::getName);
                break;
            case 2: // coordinateX
                comparator = Comparator.comparing(p -> p.getCoordinates().getX());
                break;
            case 3: // coordinateY
                comparator = Comparator.comparing(p -> p.getCoordinates().getY());
                break;
            case 4: // creationDate
                comparator = Comparator.comparing(Person::getCreationDate);
                break;
            case 5: // height
                comparator = Comparator.comparingDouble(Person::getHeight);
                break;
            case 6: // weight
                comparator = Comparator.comparingDouble(Person::getWeight);
                break;
            case 7: // eyeColor
                comparator = Comparator.comparing(Person::getEyeColor);
                break;
            case 8: // nationality
                comparator = Comparator.comparing(Person::getNationality);
                break;
            case 9: // LocationX
                comparator = Comparator.comparing(p -> p.getLocation().getX());
                break;
            case 10: // LocationY
                comparator = Comparator.comparing(p -> p.getLocation().getY());
                break;
            case 11: // LocationZ
                comparator = Comparator.comparing(p -> p.getLocation().getZ());
                break;
            case 12: // creator
                comparator = Comparator.comparing(Person::getCreator);
                break;
            default:
                throw new IllegalArgumentException("Invalid column index");
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        filteredData = filteredData.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        fireTableDataChanged();
    }

    public void filter(String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();
        filteredData = dataList.stream()
                .filter(person -> Long.valueOf(person.getId()).toString().contains(lowerCaseKeyword) ||
                        person.getName().toLowerCase().contains(lowerCaseKeyword) ||
                        Float.valueOf(person.getCoordinates().getX()).toString().contains(lowerCaseKeyword) ||
                        person.getCoordinates().getY().toString().contains(lowerCaseKeyword) ||
                        person.getCreationDate().toString().contains(lowerCaseKeyword) ||
                        person.getHeight().toString().contains(lowerCaseKeyword) ||
                        Double.valueOf(person.getWeight()).toString().contains(lowerCaseKeyword) ||
                        person.getEyeColor().toString().toLowerCase().contains(lowerCaseKeyword) ||
                        person.getNationality().toString().toLowerCase().contains(lowerCaseKeyword) ||
                        person.getLocation().getX().toString().contains(lowerCaseKeyword) ||
                        person.getLocation().getY().toString().contains(lowerCaseKeyword) ||
                        Integer.valueOf(person.getLocation().getZ()).toString().contains(lowerCaseKeyword) ||
                        Integer.valueOf(person.getCreator()).toString().contains(lowerCaseKeyword))
                .collect(Collectors.toList());
        fireTableDataChanged();
    }

    public void resetFilter() {
        filteredData = new ArrayList<>(dataList);
        fireTableDataChanged();
    }
}
