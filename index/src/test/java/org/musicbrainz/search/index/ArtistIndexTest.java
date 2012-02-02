package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.LuceneVersion;

import java.sql.Statement;


public class ArtistIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();
    }



    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir,ArtistIndexField.class);
        ArtistIndex ai = new ArtistIndex(conn);
        CommonTables ct = new CommonTables(conn, ai.getName());
        ct.createTemporaryTables(false);
        ai.init(writer, false);
        ai.addMetaInformation(writer);
        ai.indexData(writer, 0, Integer.MAX_VALUE);
        ai.destroy();
        writer.close();

    }

    private void addArtistOne() throws Exception {

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Farming Incident')");
        stmt.addBatch("INSERT INTO artist (id, name, gid, sort_name, begin_date_year, begin_date_month, type, gender, country,ipi_code)" +
            " VALUES (521316, 1, '4302e264-1cf0-4d1f-aca7-2a6f89e34b36', 1, 1999, 4, 2, 1, 1,'10001')");
        stmt.addBatch("INSERT INTO country (id, iso_code, name) VALUES (1, 'AF', 'Afghanistan')");

        stmt.executeBatch();
        stmt.close();
    }


    private void addArtistTwo() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3, 'Echo & The Bunnyman')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (4, 'Echo And The Bunnymen')");

        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (1, 16153, 2)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (2, 16153, 3)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (3, 16153, 4)");

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (5, 'Bunnymen Orchestra')");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 5)");

        //This is same as alias, so should be ignored
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 3)");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sort_name, comment, begin_date_year, end_date_year, type)" +
                   " VALUES (16153, 1, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 'a comment', 1978, 1995, 2)");
        stmt.executeBatch();
        stmt.close();
    }

    private void addArtistThree() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Siobhan Lynch')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Lynch, Siobhan')");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sort_name,)" +
            " VALUES (76834, 1, 'ae8707b6-684c-4d4a-95c5-d117970a6dfe', 2)");

        stmt.addBatch("INSERT INTO tag (id, name, ref_count) VALUES (1, 'Goth', 2)");
        stmt.addBatch("INSERT INTO artist_tag (artist, tag, count) VALUES (76834, 1, 10)");
        stmt.executeBatch();
        stmt.close();
    }

    private void addArtistFour() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Siobhan Lynch')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Lynch, Siobhan')");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sort_name, type)" +
            " VALUES (76834, 1, 'ae8707b6-684c-4d4a-95c5-d117970a6dfe', 2, 1)");

        stmt.addBatch("INSERT INTO tag (id, name, ref_count) VALUES (1, 'Goth', 2)");
        stmt.addBatch("INSERT INTO artist_tag (artist, tag, count) VALUES (76834, 1, 10)");
        stmt.executeBatch();
        stmt.close();
    }

    /**
     * Checks fields are indexed correctly for artist with no alias
     *
     * @throws Exception exception
     */
    public void testIndexArtistWithNoAlias() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFieldables(ArtistIndexField.ALIAS.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.ARTIST_ID.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.SORTNAME.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.BEGIN.getName()).length);
            assertEquals(0, doc.getFieldables(ArtistIndexField.END.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.COMMENT.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.TYPE.getName()).length);

            assertEquals("Farming Incident", doc.getFieldable(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.getFieldable(ArtistIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals("Farming Incident", doc.getFieldable(ArtistIndexField.SORTNAME.getName()).stringValue());
            assertEquals("1999-04", doc.getFieldable(ArtistIndexField.BEGIN.getName()).stringValue());
            assertEquals("Group", doc.getFieldable(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();

    }


    public void testIndexArtistWithType() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ArtistIndexField.TYPE.getName()).length);
            assertEquals("Group", doc.getFieldable(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }


    public void testIndexArtistWithComment() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ArtistIndexField.COMMENT.getName()).length);
            assertEquals("a comment", doc.getFieldable(ArtistIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();
    }

    public void testIndexArtistWithCountry() throws Exception {

            addArtistOne();
            RAMDirectory ramDir = new RAMDirectory();
            createIndex(ramDir);

            IndexReader ir = IndexReader.open(ramDir, true);
            assertEquals(2, ir.numDocs());
            {
                Document doc = ir.document(1);
                assertEquals(1, doc.getFieldables(ArtistIndexField.COUNTRY.getName()).length);
                assertEquals("af", doc.getFieldable(ArtistIndexField.COUNTRY.getName()).stringValue());
            }
            ir.close();
        }



    public void testIndexArtistWithIPI() throws Exception {

                addArtistOne();
                RAMDirectory ramDir = new RAMDirectory();
                createIndex(ramDir);

                IndexReader ir = IndexReader.open(ramDir, true);
                assertEquals(2, ir.numDocs());
                {
                    Document doc = ir.document(1);
                    assertEquals(1, doc.getFieldables(ArtistIndexField.IPI.getName()).length);
                    assertEquals("10001", doc.getFieldable(ArtistIndexField.IPI.getName()).stringValue());
                }
                ir.close();
            }


    public void testIndexArtistWithNoCountry() throws Exception {

            addArtistTwo();
            RAMDirectory ramDir = new RAMDirectory();
            createIndex(ramDir);

            IndexReader ir = IndexReader.open(ramDir, true);
            assertEquals(2, ir.numDocs());
            {
                Document doc = ir.document(1);
                assertEquals(1, doc.getFieldables(ArtistIndexField.COUNTRY.getName()).length);
                assertEquals("unknown", doc.getFieldable(ArtistIndexField.COUNTRY.getName()).stringValue());
            }
            ir.close();
        }
    public void testIndexArtistWithGender() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ArtistIndexField.GENDER.getName()).length);
            assertEquals("male", doc.getFieldable(ArtistIndexField.GENDER.getName()).stringValue());
        }
        ir.close();
    }

    public void testIndexArtistPersonWithUnknownGender() throws Exception {

        addArtistFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ArtistIndexField.GENDER.getName()).length);
            assertEquals("unknown", doc.getFieldable(ArtistIndexField.GENDER.getName()).stringValue());
        }
        ir.close();
    }

    public void testIndexGroupWithNoGender() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFieldables(ArtistIndexField.GENDER.getName()).length);
        }
        ir.close();
    }



    /**
     * Checks fields are indexed correctly for artist with alias and artistCredit (the aliases are not stored)
     *
     * @throws Exception exception
     */
    public void testIndexArtistWithAlias() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(4, doc.getFieldables(ArtistIndexField.ALIAS.getName()).length); //aliases are searchable but not stored
            assertEquals(1, doc.getFieldables(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.ARTIST_ID.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.SORTNAME.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.BEGIN.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.END.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.COMMENT.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.TYPE.getName()).length);

            assertEquals("Echo & The Bunnymen", doc.getFieldable(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.getFieldable(ArtistIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals("Echo & The Bunnymen", doc.getFieldable(ArtistIndexField.SORTNAME.getName()).stringValue());
            assertEquals("1978", doc.getFieldable(ArtistIndexField.BEGIN.getName()).stringValue());
            assertEquals("Group", doc.getFieldable(ArtistIndexField.TYPE.getName()).stringValue());
            assertEquals("a comment", doc.getFieldable(ArtistIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception exception
     */
    public void testBeginDate() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ArtistIndexField.BEGIN.getName()).length);
            assertEquals("1978", doc.getFieldable(ArtistIndexField.BEGIN.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception exception
     */
    public void testEndDate() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ArtistIndexField.END.getName()).length);
            assertEquals("1995", doc.getFieldable(ArtistIndexField.END.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks record with type = null is set to unknown
     *
     * @throws Exception  exception
     */
    public void testIndexArtistWithNoType() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ArtistIndexField.TYPE.getName()).length);
            assertEquals("unknown", doc.getFieldable(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * Checks record with comment = null is not indexed
     *
     * @throws Exception  exception
     */
    public void testIndexArtistWithNoComment() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ArtistIndexField.COMMENT.getName()).length);
            assertEquals("-", doc.getFieldable(ArtistIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * Checks record with begin date = null is not indexed
     *
     * @throws Exception  exception
     */
    public void testIndexArtistWithNoBeginDate() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFieldables(ArtistIndexField.BEGIN.getName()).length);
        }
        ir.close();
    }


    /**
     * Checks record with end date = null is not indexed
     *
     * @throws Exception  exception
     */
    public void testIndexArtistWithNoEndDate() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFieldables(ArtistIndexField.END.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.COMMENT.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.TYPE.getName()).length);

            assertEquals("Siobhan Lynch", doc.getFieldable(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("ae8707b6-684c-4d4a-95c5-d117970a6dfe", doc.getFieldable(ArtistIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals("Lynch, Siobhan", doc.getFieldable(ArtistIndexField.SORTNAME.getName()).stringValue());
            assertEquals("unknown", doc.getFieldable(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks fields with different sort name to name is indexed correctly
     *
     * @throws Exception  exception
     */
    public void testIndexArtistWithDifferentSortName() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.SORTNAME.getName()).length);
            assertEquals("Siobhan Lynch", doc.getFieldable(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("Lynch, Siobhan", doc.getFieldable(ArtistIndexField.SORTNAME.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * Checks fields with different sort name to name is indexed correctly
     *
     * @throws Exception exception
     */
    public void testIndexArtistWithTag() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFieldables(ArtistIndexField.TAG.getName()).length);
            assertEquals("Goth", doc.getFieldable(ArtistIndexField.TAG.getName()).stringValue());
            assertEquals(1, doc.getFieldables(ArtistIndexField.TAGCOUNT.getName()).length);
            assertEquals("10", doc.getFieldable(ArtistIndexField.TAGCOUNT.getName()).stringValue());
        }
        ir.close();
    }


    public void testGetTypeByDbId () throws Exception {        
        assertEquals(ArtistType.PERSON,ArtistType.getBySearchId(1));
    }
    
    public void testMetaInformation() throws Exception {
    	
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals("42459", doc.getFieldable(MetaIndexField.REPLICATION_SEQUENCE.getName()).stringValue());
            assertEquals("12", doc.getFieldable(MetaIndexField.SCHEMA_SEQUENCE.getName()).stringValue());
        }
        
    	
    }
}