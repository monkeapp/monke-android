package io.monke.app.ime.screens.share;

public class ShareItem {

    public String title;
    public String meta;

    public ShareItem(String title) {
        this.title = title;
    }

    public ShareItem(String title, String meta) {
        this(title);
        this.meta = meta;
    }
}
