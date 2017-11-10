package net.pykaso.zonkystats.zonkystats.api;

/**
 * Lite POJO for Marketspace API results
 */
public class LoansResponse {

    public String datePublished;
    public boolean published;
    public boolean covered;

    public LoansResponse(String date, boolean pub, boolean cov){
        this.datePublished = date;
        this.published = pub;
        this.covered = cov;
    }
}
