package seers.irutils.entity;
// Generated Nov 18, 2015 8:59:09 AM by Hibernate Tools 4.3.1.Final

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * BugAndFiles generated by hbm2java
 */
@Entity
@Table(name = "bug_and_files", catalog = "swt")
public class BugAndFiles implements java.io.Serializable {

	private Integer id;
	private int bugId;
	private String summary;
	private String description;
	private String bagOfWordStemmed;
	private String summaryStemmed;
	private String descriptionStemmed;
	private Date reportTime;
	private float reportTimestamp;
	private String status;
	private String commit;
	private float commitTimestamp;
	private String files;

	public BugAndFiles() {
	}

	public BugAndFiles(int bugId, String summary, String description, String bagOfWordStemmed, String summaryStemmed,
			String descriptionStemmed, Date reportTime, float reportTimestamp, String status, String commit,
			float commitTimestamp, String files) {
		this.bugId = bugId;
		this.summary = summary;
		this.description = description;
		this.bagOfWordStemmed = bagOfWordStemmed;
		this.summaryStemmed = summaryStemmed;
		this.descriptionStemmed = descriptionStemmed;
		this.reportTime = reportTime;
		this.reportTimestamp = reportTimestamp;
		this.status = status;
		this.commit = commit;
		this.commitTimestamp = commitTimestamp;
		this.files = files;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "bug_id", nullable = false)
	public int getBugId() {
		return this.bugId;
	}

	public void setBugId(int bugId) {
		this.bugId = bugId;
	}

	@Column(name = "summary", nullable = false)
	public String getSummary() {
		return this.summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Column(name = "description", nullable = false, length = 65535)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "bag_of_word_stemmed", nullable = false, length = 16777215)
	public String getBagOfWordStemmed() {
		return this.bagOfWordStemmed;
	}

	public void setBagOfWordStemmed(String bagOfWordStemmed) {
		this.bagOfWordStemmed = bagOfWordStemmed;
	}

	@Column(name = "summary_stemmed", nullable = false, length = 65535)
	public String getSummaryStemmed() {
		return this.summaryStemmed;
	}

	public void setSummaryStemmed(String summaryStemmed) {
		this.summaryStemmed = summaryStemmed;
	}

	@Column(name = "description_stemmed", nullable = false, length = 16777215)
	public String getDescriptionStemmed() {
		return this.descriptionStemmed;
	}

	public void setDescriptionStemmed(String descriptionStemmed) {
		this.descriptionStemmed = descriptionStemmed;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "report_time", nullable = false, length = 19)
	public Date getReportTime() {
		return this.reportTime;
	}

	public void setReportTime(Date reportTime) {
		this.reportTime = reportTime;
	}

	@Column(name = "report_timestamp", nullable = false, precision = 12, scale = 0)
	public float getReportTimestamp() {
		return this.reportTimestamp;
	}

	public void setReportTimestamp(float reportTimestamp) {
		this.reportTimestamp = reportTimestamp;
	}

	@Column(name = "status", nullable = false, length = 50)
	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Column(name = "commit", nullable = false, length = 50)
	public String getCommit() {
		return this.commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	@Column(name = "commit_timestamp", nullable = false, precision = 12, scale = 0)
	public float getCommitTimestamp() {
		return this.commitTimestamp;
	}

	public void setCommitTimestamp(float commitTimestamp) {
		this.commitTimestamp = commitTimestamp;
	}

	@Column(name = "files", nullable = false, length = 16777215)
	public String getFiles() {
		return this.files;
	}

	public void setFiles(String files) {
		this.files = files;
	}

}