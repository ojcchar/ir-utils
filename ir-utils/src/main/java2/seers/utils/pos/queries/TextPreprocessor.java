package seers.utils.pos.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class TextPreprocessor {

	static StanfordCoreNLP pipeline;
	private static StanfordCoreNLP pipelineLemma;

	static {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit");
		pipeline = new StanfordCoreNLP(props);

		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
		pipelineLemma = new StanfordCoreNLP(props);
	}

	public static List<String> tokenize(String text) {
		Annotation document = new Annotation(text);
		pipeline.annotate(document);

		List<String> tokens = new ArrayList<>();

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				tokens.add(word);
			}
		}

		return tokens;
	}

	public static List<String> removeNonLiterals(List<String> tokens) {
		List<String> validTokens = new ArrayList<>();
		for (String token : tokens) {
			String tokenNoLiterals = token.replaceAll("[^a-zA-Z0-9]", " ");
			List<String> newTokens = tokenize(tokenNoLiterals);
			validTokens.addAll(newTokens);
		}
		return validTokens;
	}

	public static List<String> removeIntegers(List<String> tokens) {
		List<String> validTokens = new ArrayList<>();
		for (String token : tokens) {
			if (!token.matches("\\d+")) {
				validTokens.add(token);
			}
		}
		return validTokens;
	}

	public static List<String> breakIdentifiers(List<String> tokens) {
		List<String> validTokens = new ArrayList<>();
		for (String token : tokens) {
			String[] tokenSplit = StringUtils.splitByCharacterTypeCamelCase(token);
			validTokens.addAll(Arrays.asList(tokenSplit));
		}
		return validTokens;
	}

	public static List<String> removeStopWords(List<String> tokens, List<String> stopWords) {
		List<String> validTokens = new ArrayList<>();
		for (String token : tokens) {
			if (!isStopWord(token, stopWords)) {
				validTokens.add(token);
			}
		}
		return validTokens;
	}

	private static boolean isStopWord(String token, List<String> stopWords) {
		for (String stopWord : stopWords) {
			if (stopWord.equalsIgnoreCase(token)) {
				return true;
			}
		}
		return false;
	}

	public static List<String> toLowerCase(List<String> tokens) {
		List<String> validTokens = new ArrayList<>();
		for (String token : tokens) {
			validTokens.add(token.toLowerCase());
		}
		return validTokens;
	}

	public static List<String> tokenizeWithLemma(String text) {
		Annotation document = new Annotation(text);
		pipelineLemma.annotate(document);

		List<String> tokens = new ArrayList<>();

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String lemma = token.get(LemmaAnnotation.class);
				tokens.add(lemma);
			}
		}

		return tokens;
	}

	static String[] PARENTHESIS = { "-LCB-", "-RCB-", "-LRB-", "-RRB-", "-LSB-", "-RSB-" };

	public static List<String> removePunctuation(List<String> tokens) {
		List<String> validTokens = new ArrayList<>();
		for (String token : tokens) {
			if (!token.matches("[\\p{P}\\p{S}]") && !isParenthesis(token)) {
				validTokens.add(token);
			}
		}
		return validTokens;
	}

	private static boolean isParenthesis(String token) {
		for (String parenthesis : PARENTHESIS) {
			if (token.contains(parenthesis)) {
				return true;
			}
		}
		return false;
	}

	public static List<String> removeBlanks(List<String> tokens) {
		List<String> validTokens = new ArrayList<>();
		for (String token : tokens) {
			if (!token.matches("[\\p{Space}]+") && !token.trim().isEmpty()) {
				validTokens.add(token);
			}
		}
		return validTokens;
	}

	public static List<String> removeShortTokens(List<String> tokens, int size) {
		List<String> validTokens = new ArrayList<>();
		for (String token : tokens) {
			if (token.length() > size) {
				validTokens.add(token);
			}
		}
		return validTokens;
	}

	public static List<String> removeSpecifiedTokens(List<String> tokens, Set<String> tokensToRemove) {
		List<String> validTokens = new ArrayList<>();
		for (String token : tokens) {
			if (!isTokenInList(token, tokensToRemove)) {
				validTokens.add(token);
			}
		}
		return validTokens;
	}

	private static boolean isTokenInList(String token, Set<String> tokensToRemove) {
		for (String t : tokensToRemove) {
			if (t.equalsIgnoreCase(token)) {
				return true;
			}
		}
		return false;
	}

	public static List<List<String>> tokenizeInSentences(String text) {
		Annotation document = new Annotation(text);
		pipeline.annotate(document);

		List<List<String>> sentencesTokens = new ArrayList<>();

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {

			List<String> tokens = new ArrayList<>();
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				tokens.add(word);
			}

			sentencesTokens.add(tokens);
		}

		return sentencesTokens;
	}

}
