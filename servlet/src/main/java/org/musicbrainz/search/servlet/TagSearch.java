package org.musicbrainz.search.servlet;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.TagIndexField;
import org.musicbrainz.search.servlet.mmd2.TagWriter;

import java.io.IOException;
import java.util.ArrayList;


public class TagSearch extends SearchServer {

    protected void setupDefaultFields() {
        defaultFields       = new ArrayList<String>();
        defaultFields.add(TagIndexField.TAG.getName());
    }

    public TagSearch() throws Exception {
    
        resultsWriter = new TagWriter();
        setupDefaultFields();
        analyzer = DatabaseIndex.getAnalyzer(TagIndexField.class);
    }

    public TagSearch(IndexSearcher searcher) throws Exception {
        this();
        indexSearcher = searcher;
    }

    public TagSearch(IndexSearcher searcher, String query, int offset, int limit) throws Exception {
        this(searcher);
        this.query=query;
        this.offset=offset;
        this.limit=limit;
    }

     @Override
    protected QueryParser getParser() {
       return new TagQueryParser(defaultFields.get(0), analyzer);
    }

    @Override
    protected  String printExplainHeader(Document doc)
            throws IOException, ParseException {
        return doc.get(TagIndexField.ID.getName()) +':'
                + doc.get(TagIndexField.TAG.getName())
                + '\n';
    }

}