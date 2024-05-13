/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.futurexskills.custom.processors.sample;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Tags({"Wikipedia", "Fetch", "Content"})
@CapabilityDescription("Fetches the content of a Wikipedia page and stores it in a FlowFile.")
@ReadsAttributes({})
@WritesAttributes({})
public class MyProcessor extends AbstractProcessor {

    public static final PropertyDescriptor MY_PROPERTY = new PropertyDescriptor
            .Builder().name("Wikipedia Page URL")
            .displayName("My property")
            .description("The URL of the Wikipedia page to fetch content from.")
            .required(true)
            .addValidator(StandardValidators.URL_VALIDATOR)
            .build();

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("Successful processing of FlowFile")
            .build();

    public static final Relationship FAILURE = new Relationship.Builder()
            .name("FAILURE")
            .description("Failed processing of FlowFile")
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        descriptors = new ArrayList<>();
        descriptors.add(MY_PROPERTY);
        descriptors = Collections.unmodifiableList(descriptors);

        relationships = new HashSet<>();
        relationships.add(SUCCESS);
        relationships.add(FAILURE);
        relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        FlowFile flowFile = session.create();

        try {
            final URL url = new URL(context.getProperty(WIKIPEDIA_PAGE_URL).getValue());
            final URLConnection connection = url.openConnection();
            connection.connect();

            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = session.write(flowFile)) {
                outputStream.write(inputStream.readAllBytes());
            }

            session.transfer(flowFile, SUCCESS);
        } catch (IOException e) {
            getLogger().error("Failed to fetch content from Wikipedia page", e);
            session.transfer(flowFile, FAILURE);
        }
    }
}
