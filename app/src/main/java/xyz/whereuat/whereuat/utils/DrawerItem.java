package xyz.whereuat.whereuat.utils;

/**
 * Created by julius on 5/10/16.
 */
public class DrawerItem {
    private String mTitle;
    private int mIcon;

    public DrawerItem(String title, int icon){
        this.mTitle = title;
        this.mIcon = icon;
    }

    public String getTitle(){ return this.mTitle; }

    public int getIcon(){ return this.mIcon; }

    public void setTitle(String title){ this.mTitle= title; }

    public void setIcon(int icon){ this.mIcon = icon; }
}
