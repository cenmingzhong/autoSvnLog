package start;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class ConfigModel {
    private String lang;
    private List<String> authors;
    private List<String> dateRange;
    private List<String> repos;
    private List<String> format;
    private List<String> includes;
    private List<String> excludes;
    private Boolean showRate;
    private String svnUserName;
    private String svnPassword;
    private String exportLogUrl;
}
