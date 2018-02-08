package com.thekirankumar.youtubeauto.bookmarks;

import io.realm.RealmObject;

/**
 * Created by kiran.kumar on 08/02/18.
 */

class OverrideSettings extends RealmObject {
    private UserAgentMode userAgentMode;
    private OverviewMode overviewMode;
    private ZoomFactor zoomFactor;
    private ToolbarMode toolbarMode;
    private ExtraScript[] extraScripts;

    public OverviewMode getOverviewMode() {
        return overviewMode;
    }

    public void setOverviewMode(OverviewMode overviewMode) {
        this.overviewMode = overviewMode;
    }

    public ZoomFactor getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(ZoomFactor zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public ToolbarMode getToolbarMode() {
        return toolbarMode;
    }

    public void setToolbarMode(ToolbarMode toolbarMode) {
        this.toolbarMode = toolbarMode;
    }

    public ExtraScript[] getExtraScripts() {
        return extraScripts;
    }

    public void setExtraScripts(ExtraScript[] extraScripts) {
        this.extraScripts = extraScripts;
    }

    public UserAgentMode getUserAgentMode() {
        return userAgentMode;
    }

    public void setUserAgentMode(UserAgentMode userAgentMode) {
        this.userAgentMode = userAgentMode;
    }
}
