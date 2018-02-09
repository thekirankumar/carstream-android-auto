package com.thekirankumar.youtubeauto.bookmarks;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by kiran.kumar on 08/02/18.
 */

public class OverrideSettings extends RealmObject {
    private UserAgentMode userAgentMode = new UserAgentMode();
    private OverviewMode overviewMode = new OverviewMode();
    private ZoomFactor zoomFactor = new ZoomFactor();
    private ToolbarMode toolbarMode = new ToolbarMode();
    private RealmList<ExtraScript> extraScripts = new RealmList<>();

    public OverrideSettings() {
    }

    public RealmList<ExtraScript> getExtraScripts() {
        return extraScripts;
    }

    public void setExtraScripts(RealmList<ExtraScript> extraScripts) {
        this.extraScripts = extraScripts;
    }

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

    public UserAgentMode getUserAgentMode() {
        return userAgentMode;
    }

    public void setUserAgentMode(UserAgentMode userAgentMode) {
        this.userAgentMode = userAgentMode;
    }
}
