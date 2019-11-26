package com.myapp2.rebecca.guardianresearchtool;


public class News {

    /**
     * Headline of the news piece
     */
    private String mHeadline;

    /**
     * Date that the news piece was written
     */
    private String mDate;

    /**
     * Section that the news piece is found in
     */
    private String mSection;

    /**
     * website address that the news piece is found in
     */
    private String mUrl;

    /**
     * thumbnail of the news piece
     */
    private String mNewsThumb;

    /**
     * Constructs a new {@link News}
     *
     * @param headline  is the title of the headline
     * @param date      is the author of the news item
     * @param section   is the section of the news that the item is found in
     * @param newsThumb is the thumbnail image of the news item
     */

    public News(String headline, String section, String date, String url, String newsThumb) {
        mHeadline = headline;
        mDate = date;
        mSection = section;
        mUrl = url;
        mNewsThumb = newsThumb;
    }

    //returns the headline
    public String getmHeadline() {
        return mHeadline;
    }

    //returns the date published
    public String getmDate() {
        return mDate;
    }

    //returns the section
    public String getmSection() {
        return mSection;
    }

    //returns the url
    public String getUrl() {
        return mUrl;
    }

    //returns the thumbnail
    public String getmNewsThumb() {
        return mNewsThumb;
    }

}