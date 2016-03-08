package org.mokey.acupple.dashcam.agent.trace.impl;

import org.mokey.acupple.dashcam.agent.trace.ISampler;

/**
 * Created by Yuan on 2015/6/17.
 */
public class AlwaysSampler implements ISampler {
    private static class AlwaysSamplerHolder {
        private static AlwaysSampler instance = new AlwaysSampler();
    }

    public static AlwaysSampler getInstance() {
        return AlwaysSamplerHolder.instance;
    }

    private AlwaysSampler() {
    }

    @Override
    public boolean next() {
        return true;
    }
}
