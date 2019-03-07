import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Configuration {
    MyLog mlog = new MyLog("Configuration", true);

    //runtime build
    HashMap<String, String> parameterMap = new HashMap<>();
    String parametersHeader = "";
    String parametersValuesString = "";

    public abstract String getHeaders();
    public abstract String getValuesString();
    protected abstract void fillParameterMap();//cannot have private abstract methods

    protected void setupStrings(){
        fillParameterMap();
        buildParameterStrings();
    }

    protected void buildParameterStrings(){
        for (Iterator<Map.Entry<String, String>> parameterIterator = parameterMap.entrySet().iterator(); parameterIterator.hasNext();){
            Map.Entry pair = parameterIterator.next();
            parametersHeader += pair.getKey();
            parametersValuesString += pair.getValue();
            if(parameterIterator.hasNext()){
                parametersHeader+=",";
                parametersValuesString += ",";
            }
        }
    }

}
