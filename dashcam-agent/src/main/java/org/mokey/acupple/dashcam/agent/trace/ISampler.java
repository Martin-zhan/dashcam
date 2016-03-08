package org.mokey.acupple.dashcam.agent.trace;

/**
 * Created by Yuan on 2015/6/17.
 */
public interface ISampler {
    /**
     * 确定是否要对一个span进行trace
     *
     * @return true or false
     */
    public boolean next();
}
