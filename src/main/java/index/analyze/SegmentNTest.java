package index.analyze;

import org.apache.lucene.codecs.StoredFieldsReader;
import org.apache.lucene.codecs.blocktree.BlockTreeTermsReader;
import org.apache.lucene.codecs.lucene50.Lucene50PostingsReader;
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.StringHelper;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.lucene.codecs.CodecUtil.footerLength;

public class SegmentNTest {

    public static void main(String[] args) throws IOException, SolrServerException {
//        FieldInfo fieldInfo = analyzeFieldInfo("products_id");
//        analyzeFieldsReader();
//        analyzeBlockTreeTermsReader();
        search();
    }

    public void analyzeTIM() throws IOException {
        Path path = Paths.get("E:\\java\\solr-7.1.0\\solr\\server\\solr\\yoins\\data\\index");
        Directory directory = FSDirectory.open(path);
        IndexInput in = directory.openInput("_1g3_Lucene50_0.tim", IOContext.READ);
        System.err.println(in.readInt());
        in.readString();

        //header校验
        in.readInt();
        byte id[] = new byte[StringHelper.ID_LENGTH];
        in.readBytes(id, 0, id.length);
        int suffixLength = in.readByte() & 0xFF;
        byte suffixBytes[] = new byte[suffixLength];
        in.readBytes(suffixBytes, 0, suffixBytes.length);
        String suffix = new String(suffixBytes, 0, suffixBytes.length, StandardCharsets.UTF_8);
        System.out.println(suffix);
        in.seek(suffixBytes.length);
        System.out.println(in.readInt());

        in.seek(in.length() - footerLength());
        long remaining = in.length() - in.getFilePointer();
        long expected = footerLength();
        final int magic = in.readInt();
        final int algorithmID = in.readInt();
    }

    public static void analyzeSI() throws IOException {
        Path path = Paths.get("E:\\java\\solr-7.1.0\\solr\\server\\solr\\yoins\\data\\index");
        Directory directory = FSDirectory.open(path);
        String[] files = directory.listAll();
        String lastSegmentsFile = SegmentInfos.getLastCommitSegmentsFileName(files);
        SegmentInfos segmentInfos = SegmentInfos.readCommit(directory, lastSegmentsFile);	//读segment_
    }

    /**
     * fnm,fdx
     * @param fieldName     字段名
     * @return              字段的相关信息
     * @throws IOException
     */
    public static FieldInfo analyzeFieldInfo(String fieldName)throws IOException{
        Path path = Paths.get("E:\\java\\solr-7.1.0\\solr\\server\\solr\\yoins\\data\\index");
        Directory directory = FSDirectory.open(path);
        String[] files = directory.listAll();
        String lastSegmentsFile = SegmentInfos.getLastCommitSegmentsFileName(files);
        SegmentInfos segmentInfos = SegmentInfos.readCommit(directory, lastSegmentsFile);
        IOContext context = IOContext.READ;
        SegmentCommitInfo segmentCommitInfo = segmentInfos.info(0);
        FieldInfos infos = segmentCommitInfo.info.getCodec().fieldInfosFormat().read(directory, segmentCommitInfo.info,"", context);
        SegmentReadState segmentReadState = new SegmentReadState(directory, segmentInfos.info(0).info, infos, context);
        FieldInfo fieldInfo = infos.fieldInfo(fieldName);
        System.out.println(fieldInfo);
        return fieldInfo;
    }

    /**
     * fdt,fdx
     * @return
     * @throws IOException
     */
    public static StoredFieldsReader analyzeFieldsReader() throws IOException{
        Path path = Paths.get("E:\\java\\solr-7.1.0\\solr\\server\\solr\\yoins\\data\\index");
        Directory directory = FSDirectory.open(path);
        String[] files = directory.listAll();
        String lastSegmentsFile = SegmentInfos.getLastCommitSegmentsFileName(files);
        SegmentInfos segmentInfos = SegmentInfos.readCommit(directory, lastSegmentsFile);
        IOContext context = IOContext.READ;
        SegmentCommitInfo segmentCommitInfo = segmentInfos.info(0);
        FieldInfos infos = segmentCommitInfo.info.getCodec().fieldInfosFormat().read(directory, segmentCommitInfo.info,"", context);
        Lucene50StoredFieldsFormat lucene50StoredFieldsFormat = new Lucene50StoredFieldsFormat();
        StoredFieldsReader storedFieldsReader = lucene50StoredFieldsFormat.fieldsReader(directory, segmentCommitInfo.info, infos, context);

        return storedFieldsReader;
    }

    /**
     * tim,pos,dcoc
     * @return
     * @throws IOException
     */
    public static BlockTreeTermsReader analyzeBlockTreeTermsReader() throws IOException{
        Path path = Paths.get("E:\\java\\solr-7.1.0\\solr\\server\\solr\\yoins\\data\\index");
        Directory directory = FSDirectory.open(path);
        String[] files = directory.listAll();
        String lastSegmentsFile = SegmentInfos.getLastCommitSegmentsFileName(files);
        SegmentInfos segmentInfos = SegmentInfos.readCommit(directory, lastSegmentsFile);
        IOContext context = IOContext.READ;
        SegmentCommitInfo segmentCommitInfo = segmentInfos.info(0);
        FieldInfos infos = segmentCommitInfo.info.getCodec().fieldInfosFormat().read(directory, segmentCommitInfo.info,"", context);
        SegmentReadState segmentReadState = new SegmentReadState(directory, segmentInfos.info(0).info, infos, context);
        SegmentReadState newSegmentReadState = new SegmentReadState(segmentReadState, "Lucene50_0");
        //doc,pos
        Lucene50PostingsReader lucene50PostingsReader = new Lucene50PostingsReader(newSegmentReadState);

        PostingsEnum postingsEnum = lucene50PostingsReader.postings(infos.fieldInfo("products_id"),lucene50PostingsReader.newTermState(), null, 0);
        BlockTreeTermsReader blockTreeTermsReader = new BlockTreeTermsReader(lucene50PostingsReader, newSegmentReadState);
        Terms terms = blockTreeTermsReader.terms("products_id");
        return blockTreeTermsReader;
    }

    public static void search() throws IOException{
        Path path = Paths.get("E:\\java\\solr-7.1.0\\solr\\server\\solr\\yoins\\data\\index");
        Directory directory = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity());
        Query query = new WildcardQuery(new Term("products_id", "*"));
        System.out.println(reader.docFreq(new Term("platform", "3")));
        List<LeafReaderContext> readers = reader.leaves();
        PostingsEnum postingsEnum = readers.get(0).reader().postings(new Term("platform", "3"));
        Map<Integer, Integer> map = new HashMap<>();
        while (postingsEnum != null) {
            int docID = postingsEnum.nextDoc();
            if (docID == PostingsEnum.NO_MORE_DOCS) {
                break;
            }else {
                int count = map.getOrDefault(docID, 0) + 1;
                map.put(docID, count);
            }
        }
        System.out.println(map);
        TopDocs docs = searcher.search(query, 10);
        for (ScoreDoc document: docs.scoreDocs){
            Document doc = reader.document(document.doc);
        }
    }
}
