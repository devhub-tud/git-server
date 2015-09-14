//package nl.tudelft.ewi.git.client;
//
//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.Entity;
//import javax.ws.rs.client.WebTarget;
//import javax.ws.rs.core.GenericType;
//import javax.ws.rs.core.MediaType;
//
//import java.util.List;
//
//import nl.tudelft.ewi.git.models.CreateRepositoryModel;
//import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
//import nl.tudelft.ewi.git.models.RepositoryModel;
//
///**
// * This class allows you query and manipulate repositories on the git-server.
// */
//public class RepositoriesImpl extends Backend implements Repositories {
//
//	private static final String BASE_PATH = "/api/repositories";
//
//	RepositoriesImpl(Client client, String host) {
//		super(client, host);
//	}
//
//	@Override
//	public List<RepositoryModel> retrieveAll() throws GitClientException {
//		return perform(new Request<List<RepositoryModel>>() {
//			@Override
//			public List<RepositoryModel> perform(WebTarget target) {
//				return target.path(BASE_PATH)
//					.request(MediaType.APPLICATION_JSON)
//					.get(new GenericType<List<RepositoryModel>>() { });
//			}
//		});
//	}
//
//	@Override
//	public RepositoryImpl retrieve(final RepositoryModel model) throws GitClientException {
//		return new RepositoryImpl(client, host, perform(new Request<DetailedRepositoryModel>() {
//			@Override
//			public DetailedRepositoryModel perform(WebTarget target) {
//				return target.path(model.getPath())
//						.request(MediaType.APPLICATION_JSON)
//						.get(DetailedRepositoryModel.class);
//			}
//		}));
//	}
//
//	@Override
//	public RepositoryImpl retrieve(final String name) throws GitClientException {
//		return new RepositoryImpl(client, host, perform(new Request<DetailedRepositoryModel>() {
//			@Override
//			public DetailedRepositoryModel perform(WebTarget target) {
//				return target.path(BASE_PATH).path(encode(name))
//						.request(MediaType.APPLICATION_JSON)
//						.get(DetailedRepositoryModel.class);
//			}
//		}));
//	}
//
//	@Override
//	public RepositoryImpl create(final CreateRepositoryModel newRepository) throws GitClientException {
//		return new RepositoryImpl(client, host, perform(new Request<DetailedRepositoryModel>() {
//			@Override
//			public DetailedRepositoryModel perform(WebTarget target) {
//				return target.path(BASE_PATH)
//						.request(MediaType.APPLICATION_JSON)
//						.post(Entity.json(newRepository), DetailedRepositoryModel.class);
//			}
//		}));
//	}
//
//}
