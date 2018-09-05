import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.util.HashMap;
import java.util.Iterator;

public class InvertedIndex {
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable>{
		private Text word = new Text();
		private IntWritable docId = new IntWritable();

		public void map(Object key, Text doc, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(doc.toString());

			// initialize docId for a doc/line
			if (itr.hasMoreTokens()) {
				docId.set(Integer.parseInt(itr.nextToken()));
			}

			while (itr.hasMoreTokens()) {
				word.set(itr.nextToken());
				context.write(word, docId);
			}
			// output pairs: (word, docId)
		}
	}

	public static class DocSumReducer extends Reducer<Text, IntWritable, Text, Text> {
		private Text result = new Text();
		public void reduce(Text word, Iterable<IntWritable> docIds, Context context) throws IOException, InterruptedException {
			// Map: docId (key) -> count (value), for certain word 
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			for (IntWritable docId : docIds) {
				int id = docId.get();
				if (map != null && map.containsKey(id)){
					map.put(id, map.get(id) + 1);
				} else {
					map.put(id, 1);
				}
			}
			
			String res = "";

			Iterator<HashMap.Entry<Integer, Integer>> entries = map.entrySet().iterator();
			if (entries.hasNext()) {
				HashMap.Entry<Integer, Integer> entry = (HashMap.Entry<Integer, Integer>)entries.next();
				res += entry.getKey().toString() + ":" + entry.getValue();
            }
			while (entries.hasNext()) {
				HashMap.Entry<Integer, Integer> entry = (HashMap.Entry<Integer, Integer>)entries.next();
				res += '\t' + entry.getKey().toString() + ":" + entry.getValue();
			}

			result.set(res);
			context.write(word, result);
			// output lines: word docID:count    docID:count    docID:count...
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "inverted index");
		job.setJarByClass(InvertedIndex.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(DocSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
