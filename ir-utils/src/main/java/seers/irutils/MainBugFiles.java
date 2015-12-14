package seers.irutils;

public class MainBugFiles {

	// private static SoftwareSystem softwareSystem = new SoftwareSystem();
	// static Object name = "swt";
	//
	// @SuppressWarnings("unchecked")
	// public static void main(String[] args) {
	//
	// softwareSystem = new SoftwareSystem();
	//
	// // GenericDao.setResourceFile("seers/irutils/entity/hibernate.cfg.xml");
	// GenericDaoImpl<BugAndFiles, Integer> bfDao = new GenericDaoImpl<>();
	// Session session = bfDao.openCurrentSession();
	// Transaction tx = session.beginTransaction();
	//
	// try {
	//
	// GenericDaoImpl<SoftwareSystem, Integer> sDao = new
	// GenericDaoImpl<>(session);
	//
	// softwareSystem = (SoftwareSystem) sDao.executeQueryUnique("From
	// SoftwareSystem s where s.name = ?", name);
	//
	// // -----------
	//
	// Long num = (Long) bfDao.executeQueryUnique("select count(id) from
	// BugAndFiles");
	// int pageSize = 50;
	//
	// System.out.println(num);
	//
	// for (int offset = 0; offset < num; offset += pageSize) {
	//
	// // get all issues, paginated
	// Query query = session.createQuery("from BugAndFiles");
	// query.setFirstResult(offset);
	// query.setMaxResults(pageSize);
	//
	// List<BugAndFiles> list = query.list();
	//
	// processList(session, list);
	//
	// System.out.println(offset + list.size());
	// }
	//
	// tx.commit();
	// } catch (Exception e) {
	// tx.rollback();
	// e.printStackTrace();
	// } finally {
	// session.close();
	// GenericDao.close();
	// }
	// }
	//
	// private static void processList(Session session, List<BugAndFiles> list)
	// {
	// GenericDaoImpl<IrRelevanceJudgement, IrRelevanceJudgementId> rjDao = new
	// GenericDaoImpl<>(session);
	// GenericDaoImpl<Revision, IrRelevanceJudgementId> rDao = new
	// GenericDaoImpl<>(session);
	// GenericDaoImpl<IrDocument, Integer> dDao = new GenericDaoImpl<>(session);
	// GenericDaoImpl<Issue, Integer> iDao = new GenericDaoImpl<>(session);
	//
	// for (BugAndFiles bf : list) {
	// String bugId = String.valueOf(bf.getBugId());
	//
	// Issue iss = (Issue) iDao.executeQueryUnique("from Issue i where i.issueId
	// = ? and i.softwareSystem = ?",
	// bugId, softwareSystem);
	// Revision revision = (Revision) rDao.executeQueryUnique(
	// "from Revision r where r.commitId = ? and r.softwareSystem = ?",
	// bf.getCommit(), softwareSystem);
	//
	// String[] files = bf.getFiles().split("\n");
	// // System.out.println(iss.getIssueId());
	// Set<IrQuery> queries = iss.getIrQueries();
	// IrQuery q = null;
	// for (IrQuery irQuery : queries) {
	// q = irQuery;
	// break;
	// }
	//
	// for (String file : files) {
	// IrDocument entity = new IrDocument(revision, file.trim(), "", "file");
	// dDao.persist(entity);
	//
	// IrRelevanceJudgement entity2 = new IrRelevanceJudgement(
	// new IrRelevanceJudgementId(entity.getId(), q.getId()), null, null);
	// rjDao.persist(entity2);
	// }
	//
	// // qDao.executeQueryUnique("from Issue i where i.", parameters)
	//
	// }
	//
	// }

}
