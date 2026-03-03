package com.sentinel.lib.service;

public interface AbuseClassifier {
    /**
     * Evaluates text for abusive content.
     * @param text Original (or redacted) text.
     * @return Score between 0.0 and 1.0 indicating abuse likelihood.
     */
    double classify(String text);
}
