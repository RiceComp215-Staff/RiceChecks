//
// This code is part of AnnoAutoGrader
// Copyright 2018, Dan S. Wallach, Rice University
// Made available subject to the Apache 2.0 License
//

package edu.rice.autograder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WordWrapTest {
    @Test
    fun basics() {
        assertEquals(listOf("Hello", "World"), wordWrap("Hello World", 4))
    }

    @Test
    fun somethingBig() {
        val declaration = "We hold these truths to be self-evident, that all men are created equal, that they are endowed by their Creator with certain unalienable Rights, that among these are Life, Liberty and the pursuit of Happiness.--That to secure these rights, Governments are instituted among Men, deriving their just powers from the consent of the governed, --That whenever any Form of Government becomes destructive of these ends, it is the Right of the People to alter or to abolish it, and to institute new Government, laying its foundation on such principles and organizing its powers in such form, as to them shall seem most likely to effect their Safety and Happiness. Prudence, indeed, will dictate that Governments long established should not be changed for light and transient causes; and accordingly all experience hath shewn, that mankind are more disposed to suffer, while evils are sufferable, than to right themselves by abolishing the forms to which they are accustomed. But when a long train of abuses and usurpations, pursuing invariably the same Object evinces a design to reduce them under absolute Despotism, it is their right, it is their duty, to throw off such Government, and to provide new Guards for their future security.--Such has been the patient sufferance of these Colonies; and such is now the necessity which constrains them to alter their former Systems of Government. The history of the present King of Great Britain is a history of repeated injuries and usurpations, all having in direct object the establishment of an absolute Tyranny over these States. To prove this, let Facts be submitted to a candid world."

        val split70 = listOf("We hold these truths to be self-evident, that all men are created",
                "equal, that they are endowed by their Creator with certain unalienable",
                "Rights, that among these are Life, Liberty and the pursuit of",
                "Happiness.--That to secure these rights, Governments are instituted",
                "among Men, deriving their just powers from the consent of the",
                "governed, --That whenever any Form of Government becomes destructive",
                "of these ends, it is the Right of the People to alter or to abolish",
                "it, and to institute new Government, laying its foundation on such",
                "principles and organizing its powers in such form, as to them shall",
                "seem most likely to effect their Safety and Happiness. Prudence,",
                "indeed, will dictate that Governments long established should not be",
                "changed for light and transient causes; and accordingly all experience",
                "hath shewn, that mankind are more disposed to suffer, while evils are",
                "sufferable, than to right themselves by abolishing the forms to which",
                "they are accustomed. But when a long train of abuses and usurpations,",
                "pursuing invariably the same Object evinces a design to reduce them",
                "under absolute Despotism, it is their right, it is their duty, to",
                "throw off such Government, and to provide new Guards for their future",
                "security.--Such has been the patient sufferance of these Colonies; and",
                "such is now the necessity which constrains them to alter their former",
                "Systems of Government. The history of the present King of Great",
                "Britain is a history of repeated injuries and usurpations, all having",
                "in direct object the establishment of an absolute Tyranny over these",
                "States. To prove this, let Facts be submitted to a candid world.")

        val split40 = listOf("We hold these truths to be self-evident,",
                "that all men are created equal, that",
                "they are endowed by their Creator with",
                "certain unalienable Rights, that among",
                "these are Life, Liberty and the pursuit",
                "of Happiness.--That to secure these",
                "rights, Governments are instituted among",
                "Men, deriving their just powers from the",
                "consent of the governed, --That whenever",
                "any Form of Government becomes",
                "destructive of these ends, it is the",
                "Right of the People to alter or to",
                "abolish it, and to institute new",
                "Government, laying its foundation on",
                "such principles and organizing its",
                "powers in such form, as to them shall",
                "seem most likely to effect their Safety",
                "and Happiness. Prudence, indeed, will",
                "dictate that Governments long",
                "established should not be changed for",
                "light and transient causes; and",
                "accordingly all experience hath shewn,",
                "that mankind are more disposed to",
                "suffer, while evils are sufferable, than",
                "to right themselves by abolishing the",
                "forms to which they are accustomed. But",
                "when a long train of abuses and",
                "usurpations, pursuing invariably the",
                "same Object evinces a design to reduce",
                "them under absolute Despotism, it is",
                "their right, it is their duty, to throw",
                "off such Government, and to provide new",
                "Guards for their future security.--Such",
                "has been the patient sufferance of these",
                "Colonies; and such is now the necessity",
                "which constrains them to alter their",
                "former Systems of Government. The",
                "history of the present King of Great",
                "Britain is a history of repeated",
                "injuries and usurpations, all having in",
                "direct object the establishment of an",
                "absolute Tyranny over these States. To",
                "prove this, let Facts be submitted to a",
                "candid world.")

        assertEquals(split70, wordWrap(declaration, 70))
        assertEquals(split40, wordWrap(declaration, 40))
    }
}
