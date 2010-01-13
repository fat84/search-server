package org.musicbrainz.search.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.musicbrainz.search.analysis.StandardUnaccentWithPosGapAnalyzer;
import org.musicbrainz.search.analysis.StripLeadingZeroAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum LabelIndexField implements IndexField {

    LABEL_ID	("laid",		Field.Store.YES,	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    LABEL		("label",		Field.Store.YES, 	Field.Index.ANALYZED),
    COUNTRY		("country",		Field.Store.YES, 	Field.Index.ANALYZED),
    CODE		("code",		Field.Store.YES, 	Field.Index.ANALYZED, new StripLeadingZeroAnalyzer()),
    ALIAS		("alias",		Field.Store.YES, 	Field.Index.ANALYZED, new StandardUnaccentWithPosGapAnalyzer()),
    SORTNAME	("sortname",	Field.Store.YES,	Field.Index.ANALYZED),
    BEGIN		("begin",		Field.Store.YES, 	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    END			("end",			Field.Store.YES, 	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),
    COMMENT		("comment",		Field.Store.YES, 	Field.Index.ANALYZED),
    TYPE		("type",		Field.Store.YES, 	Field.Index.NOT_ANALYZED, new KeywordAnalyzer()),;

    private String name;
	private Field.Store store;
    private Field.Index index;
    private Analyzer analyzer;

    private LabelIndexField(String name, Field.Store store, Field.Index index) {
        this.name = name;
        this.store = store;
        this.index = index;
    }

    private LabelIndexField(String name, Field.Store store, Field.Index index, Analyzer analyzer) {
        this(name, store, index);
        this.analyzer = analyzer;
    }
    
    public String getName() {
        return name;
    }

    public Field.Store getStore() {
		return store;
	}

	public Field.Index getIndex() {
		return index;
	}

    public Analyzer getAnalyzer() {
        return analyzer;
    }

}

