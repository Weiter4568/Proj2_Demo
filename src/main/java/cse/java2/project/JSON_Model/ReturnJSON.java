package cse.java2.project.JSON_Model;

import java.util.List;

public class ReturnJSON {
    List<String> name;
    List<Double> value;

    public ReturnJSON(List<String> names, List<Double> values) {
        this.name = names;
        this.value = values;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public List<Double> getValue() {
        return value;
    }

    public void setValue(List<Double> value) {
        this.value = value;
    }
}
