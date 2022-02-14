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

package org.apache.shardingsphere.test.integration.framework.container.atomic;

import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * Atomic containers.
 */
@RequiredArgsConstructor
public final class AtomicContainers implements AutoCloseable {
    
    private final String scenario;
    
    private final Network network = Network.newNetwork();
    
    private final Collection<AtomicContainer> containers = new LinkedList<>();
    
    private volatile boolean started;
    
    /**
     * Register container.
     * 
     * @param container container to be registered
     * @param containerType container type
     * @param <T> type of ShardingSphere container
     * @return registered container
     */
    public <T extends AtomicContainer> T registerContainer(final T container, final String containerType) {
        container.setNetwork(network);
        container.setNetworkAliases(Collections.singletonList(String.join(".", containerType.toLowerCase(), scenario, "host")));
        String loggerName = String.join(":", container.getName(), container.getContainerId());
        container.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(loggerName), true));
        containers.add(container);
        return container;
    }
    
    /**
     * Start containers.
     */
    public void start() {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    containers.stream().filter(each -> !each.isCreated()).forEach(AtomicContainer::start);
                    waitUntilReady();
                    started = true;
                }
            }
        }
    }
    
    private void waitUntilReady() {
        containers.stream()
                .filter(each -> {
                    try {
                        return !each.isHealthy();
                        // CHECKSTYLE:OFF
                    } catch (final RuntimeException ex) {
                        // CHECKSTYLE:ON
                        return false;
                    }
                })
                .forEach(each -> {
                    while (!(each.isRunning() && each.isHealthy())) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(200L);
                        } catch (final InterruptedException ignored) {
                        }
                    }
                });
    }
    
    @Override
    public void close() {
        containers.forEach(AtomicContainer::close);
    }
}
