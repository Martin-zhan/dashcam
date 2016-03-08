package org.mokey.acupple.dashcam.agent;

import org.mokey.acupple.dashcam.common.utils.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by enousei on 8/17/15.
 */
public class TagBuilder {
    private Map<String, String> tags;

    private TagBuilder(){
        this.tags = new HashMap<>();
    }

    public static TagBuilder create(){
        return new TagBuilder();
    }

    /**
     * Append the key-value pair tag
     * @param key
     * @param value
     * @return
     */
    public TagBuilder append(String key, Object value){
        if(!Strings.isNullOrEmpty(key)){
            this.tags.put(key, value == null ? "NA": value.toString());
        }
        return this;
    }

    /**
     * Append other tags
     * @param attrs
     * @return
     */
    public TagBuilder append(Map<String, String> attrs){
        if(attrs != null){
            for (String key : attrs.keySet()){
                this.tags.put(key, attrs.get(key));
            }
        }

        return this;
    }

    /**
     * Tag1=xx,Tag2=yy,Tag3=zz
     * @param tagStr
     * @return
     */
    public TagBuilder append(String tagStr){
        if(!Strings.isNullOrEmpty(tagStr)){
            String[] tgs = tagStr.split(",");
            for (String tg: tgs){
                String[] kvs = tg.split("=");
                if(kvs.length == 2){
                    this.tags.put(kvs[0], kvs[1]);
                }
            }
        }
        return this;
    }

    public Map<String, String> build(){
        return tags;
    }
}
