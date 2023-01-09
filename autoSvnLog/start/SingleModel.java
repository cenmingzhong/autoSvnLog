package start;

import lombok.Data;

@Data
public class SingleModel {
    private static SingleModel instance = new SingleModel();
    private SingleModel() {}
    public static SingleModel getInstance() {
        return instance;
    }

    private ConfigModel configModel;
}
