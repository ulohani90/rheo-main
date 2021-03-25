package com.rheotv.android.data.network.models.useProfile.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StreamerLevelResponseBody {


    @SerializedName("data")
    @Expose
    LevelData data;

    public LevelData getData() {
        return data;
    }

    public void setData(LevelData data) {
        this.data = data;
    }

    public class LevelData {

        @SerializedName("current_level_data")
        @Expose
        List<StreamerLevel> levelData;

        @SerializedName("current_level")
        @Expose
        String currentLevel;

        @SerializedName("state")
        @Expose
        String state;

        @SerializedName("show_warning")
        @Expose
        boolean showWarning;

        @SerializedName("definition")
        @Expose
        private String definition;

        public List<StreamerLevel> getLevelData() {
            return levelData;
        }

        public void setLevelData(List<StreamerLevel> levelData) {
            this.levelData = levelData;
        }

        public String getCurrentLevel() {
            return currentLevel;
        }

        public void setCurrentLevel(String currentLevel) {
            this.currentLevel = currentLevel;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public boolean isShowWarning() {
            return showWarning;
        }

        public void setShowWarning(boolean showWarning) {
            this.showWarning = showWarning;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }
    }


}
