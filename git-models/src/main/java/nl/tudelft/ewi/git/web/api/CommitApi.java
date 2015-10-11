package nl.tudelft.ewi.git.web.api;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.EntryType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
public interface CommitApi extends DiffableApi {

	/**
	 * Base path.
	 */
	String EMPTY_PATH = "";

	/**
	 * @return fetch the model for the commit.
	 */
	@GET
	DetailedCommitModel get();

	/**
	 * Retrieve a blame result for a file.
	 *
	 * @param filePath Path for the file to blame.
	 * @return Blame model for the file.
	 */
	@GET
	@Path("blame/{path:.*}")
	BlameModel blame(@PathParam("path") String filePath);

	/**
	 * Get the entries in the root folder.
	 * @return  Entries in the folder.
	 */
	@GET
	@Path("tree")
	default Map<String, EntryType> showTree() {
		return showTree(EMPTY_PATH);
	}

	/**
	 * Show the entries in a folder.
	 * @param path Folder path.
	 * @return Entries in the folder.
	 */
	@GET
	@Path("tree/{path:.*}")
	Map<String, EntryType> showTree(@PathParam("path") String path);

	/**
	 * Open a file from the repository.
	 * @param path File path.
	 * @return Stream for the file.
	 */
	@GET
	@Path("file/{path:.*}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	InputStream showFile(@PathParam("path") String path);

	/**
	 * Read a text file using the {@link Charsets#UTF_8 UTF-8} encoding.
	 * @param path File path.
	 * @return File contents.
	 * @throws IOException if an I/O error occurs.
	 */
	@GET
	@Path("text/{path:.*}")
	@Produces(MediaType.TEXT_PLAIN)
	default String showTextFile(String path) throws IOException {
		return showTextFile(path, Charsets.UTF_8);
	}

	/**
	 *  Read a text file using the specified encoding.
	 * @param path File path.
	 * @param charset {@link Charset} to use.
	 * @return File contents.
	 * @throws IOException if an I/O error occurs.
	 */
	@GET
	@Path("text-with-charset/{path:.*}")
	@Produces(MediaType.TEXT_PLAIN)
	default String showTextFile(String path, Charset charset) throws IOException {
		return CharStreams.toString(new InputStreamReader(showFile(path), charset));
	}

}
