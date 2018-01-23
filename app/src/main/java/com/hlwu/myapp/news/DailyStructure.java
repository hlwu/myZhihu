package com.hlwu.myapp.news;

import java.util.Arrays;

/**
 * Created by hlwu on 1/3/18.
 */

public class DailyStructure {
    private String date;
    private Stories[] stories;
    private TopStories[] top_stories;

    public DailyStructure(String date, Stories[] stories, TopStories[] top_stories) {
        this.date = date;
        this.stories = stories;
        this.top_stories = top_stories;
    }

    public boolean isSameWith(DailyStructure ds) {
        if (ds == null) {
            return false;
        }
        if (!this.date.equals(ds.getDate())) {
            return false;
        }
        if (this.stories.length != ds.getStories().length) {
            return false;
        } else {
            for (int i = 0; i < this.stories.length; i++) {
                if (!this.stories[i].isSameWith(ds.getStories()[i])) {
                    return false;
                }
            }
        }
        if ((this.top_stories == null && ds.getTopStories() != null) ||
                (this.top_stories != null && ds.getTopStories() == null)) {
            return false;
        } else if (this.top_stories == null && ds.getTopStories() == null) {
            return true;
        } else {
            if (this.top_stories.length != ds.getTopStories().length) {
                return false;
            } else {
                for (int i = 0; i < this.top_stories.length; i++) {
                    if (!this.top_stories[i].isSameWith(ds.getTopStories()[i])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isTopStoriesUpdated(DailyStructure ds) {
        if (ds == null) {
            return true;
        }
        if ((this.top_stories == null && ds.getTopStories() != null) ||
                (this.top_stories != null && ds.getTopStories() == null)) {
            return true;
        } else if (this.top_stories == null && ds.getTopStories() == null) {
            return false;
        } else {
            if (this.top_stories.length != ds.getTopStories().length) {
                return true;
            } else {
                for (int i = 0; i < this.top_stories.length; i++) {
                    if (!this.top_stories[i].isSameWith(ds.getTopStories()[i])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setTopStories(TopStories[] topStories) {
        this.top_stories = topStories;
    }

    public TopStories[] getTopStories() {

        return top_stories;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStories(Stories[] stories) {
        this.stories = stories;
    }

    public String getDate() {

        return date;
    }

    public Stories[] getStories() {
        return stories;
    }

    @Override
    public String toString() {
        return "DailyStructure{" +
                "date='" + date + '\'' +
                ", stories=" + Arrays.toString(stories) +
                ", top_stories=" + Arrays.toString(top_stories) +
                '}';
    }
}
