import junit.framework.TestCase;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.musicbrainz.search.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Test retrieving artist from index and Outputting as Xml
 */
public class FindArtistTest extends TestCase {

    private SearchServer ss;

    public FindArtistTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {

        RAMDirectory ramDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);

        ArtistIndex ai = new ArtistIndex();

        //General Purpose Artist
        {
            Document doc = new Document();
            ai.addArtistGidToDocument(doc, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            ai.addArtistToDocument(doc, "Farming Incident");
            ai.addSortNameToDocument(doc, "Farming Incident");
            ai.addBeginDateToDocument(doc, "1999-04");
            ai.addTypeToDocument(doc, ArtistType.GROUP);
            writer.addDocument(doc);
        }

        //Artist with & on name and aliases
        {
            Document doc = new Document();
            ai.addArtistGidToDocument(doc, "ccd4879c-5e88-4385-b131-bf65296bf245");
            ai.addArtistToDocument(doc, "Echo & The Bunnymen");
            ai.addSortNameToDocument(doc, "Echo & The Bunnymen");
            ai.addBeginDateToDocument(doc, "1978");
            ai.addTypeToDocument(doc, ArtistType.GROUP);
            ai.addAliasToDocument(doc,"Echo And The Bunnymen");
            ai.addAliasToDocument(doc,"Echo & The Bunnyman");
            ai.addAliasToDocument(doc,"Echo and The Bunymen");
            ai.addAliasToDocument(doc,"Echo & The Bunymen");
            writer.addDocument(doc);
        }

        writer.close();
        Map<String, IndexSearcher> searchers = new HashMap<String, IndexSearcher>();
        searchers.put("artist", new IndexSearcher(ramDir));
        ss = new SearchServer(searchers);
    }

    public void testFindArtistByName() throws Exception {
        Results res = ss.search("artist", "artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexFieldName.ARTIST_ID.getFieldname()));
        assertEquals("Farming Incident", doc.get(ArtistIndexFieldName.ARTIST.getFieldname()));
        assertEquals("1999-04", doc.get(ArtistIndexFieldName.BEGIN.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.END.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.ALIAS.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.COMMENT.getFieldname()));
        assertEquals("Farming Incident", doc.get(ArtistIndexFieldName.SORTNAME.getFieldname()));
        assertEquals("group", doc.get(ArtistIndexFieldName.TYPE.getFieldname()));
    }


    public void testFindArtistBySortName() throws Exception {
        Results res = ss.search("artist", "sortname:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexFieldName.ARTIST_ID.getFieldname()));
        assertEquals("Farming Incident", doc.get(ArtistIndexFieldName.ARTIST.getFieldname()));
        assertEquals("1999-04", doc.get(ArtistIndexFieldName.BEGIN.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.END.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.ALIAS.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.COMMENT.getFieldname()));
        assertEquals("Farming Incident", doc.get(ArtistIndexFieldName.SORTNAME.getFieldname()));
        assertEquals("group", doc.get(ArtistIndexFieldName.TYPE.getFieldname()));
    }


    public void testFindArtistByType() throws Exception {
        Results res = ss.search("artist", "type:\"group\"", 0, 10);
        assertEquals(2, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexFieldName.ARTIST_ID.getFieldname()));
        assertEquals("Farming Incident", doc.get(ArtistIndexFieldName.ARTIST.getFieldname()));
        assertEquals("1999-04", doc.get(ArtistIndexFieldName.BEGIN.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.END.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.ALIAS.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.COMMENT.getFieldname()));
        assertEquals("Farming Incident", doc.get(ArtistIndexFieldName.SORTNAME.getFieldname()));
        assertEquals("group", doc.get(ArtistIndexFieldName.TYPE.getFieldname()));
    }

    public void testFindArtistByBeginDate() throws Exception {
        Results res = ss.search("artist", "begin:\"1999-04\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexFieldName.ARTIST_ID.getFieldname()));
        assertEquals("Farming Incident", doc.get(ArtistIndexFieldName.ARTIST.getFieldname()));
        assertEquals("1999-04", doc.get(ArtistIndexFieldName.BEGIN.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.END.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.ALIAS.getFieldname()));
        assertNull(doc.get(ArtistIndexFieldName.COMMENT.getFieldname()));
        assertEquals("Farming Incident", doc.get(ArtistIndexFieldName.SORTNAME.getFieldname()));
        assertEquals("group", doc.get(ArtistIndexFieldName.TYPE.getFieldname()));
    }

    public void testFindArtistByEndDate() throws Exception {
        Results res = ss.search("artist", "end:\"1999-04\"", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testFindArtistByTypeNoMatch() throws Exception {
        Results res = ss.search("artist", "type:\"person\"", 0, 10);
        assertEquals(0, res.totalHits);
    }

    public void testFindArtistByAlias() throws Exception {
        Results res = ss.search("artist", "alias:\"Echo And The Bunnymen\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        Document doc = result.doc;
        assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.get(ArtistIndexFieldName.ARTIST_ID.getFieldname()));
        assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexFieldName.ARTIST.getFieldname()));
        assertEquals("1978", doc.get(ArtistIndexFieldName.BEGIN.getFieldname()));
        assertEquals("Echo & The Bunnymen", doc.get(ArtistIndexFieldName.SORTNAME.getFieldname()));
        assertEquals("group", doc.get(ArtistIndexFieldName.TYPE.getFieldname()));
    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/artist/?type=xml&query=%22Farming%20Incident%22
     *
     * @throws Exception
     */
    public void testOutputAsXml() throws Exception {

        Results res = ss.search("artist", "artist:\"Farming Incident\"", 0, 1);
        ResultsWriter writer = new ArtistXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();

        String output = sw.toString();
//      System.out.println("Xml is" + output);
//        assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));  group comes before id in output
//        assertTrue(output.contains("<artist-list count=\"1\" offset=\"0\">"));               offset comes before count in output
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Group\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Farming Incident</sort-name>"));
        assertTrue(output.contains("<life-span begin=\"1999-04\""));
        assertFalse(output.contains("end"));
        assertFalse(output.contains("alias"));
        assertFalse(output.contains("disambugation"));

    }


    /**
     * Tests that & is converted to valid xml
     *
     * @throws Exception
     */
    public void testOutputAsXmlSpecialCharacters() throws Exception {

        Results res = ss.search("artist", "alias:\"Echo And The Bunnymen\"", 0, 1);
        ResultsWriter writer = new ArtistXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();

        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("type=\"Group\""));
        assertTrue(output.contains("<name>Echo &amp; The Bunnymen</name>"));
    }


    public void testWritingPerformance() throws Exception {
        Results res = ss.search("artist", "artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        
        Date start = new Date();
        ResultsWriter writer = new ArtistXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        for(int i=0;i<1000;i++)
        {
            writer.write(pr, res);
        }
        pr.close();
        Date end = new Date();
        System.out.println("Time Taken:"+(end.getTime() - start.getTime()) + "ms");

    }

}