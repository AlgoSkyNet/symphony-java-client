/*
 *
 * Copyright 2016 The Symphony Software Foundation
 *
 * Licensed to The Symphony Software Foundation (SSF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.symphonyoss.client.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.exceptions.DataFeedException;
import org.symphonyoss.symphony.agent.model.Datafeed;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageList;
import org.symphonyoss.symphony.agent.model.V2BaseMessage;
import org.symphonyoss.symphony.clients.model.SymMessage;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frank Tarsillo on 5/21/2016.
 */
class MessageFeedWorker implements Runnable {

    private final DataFeedListener dataFeedListener;
    private final SymphonyClient symClient;
    private final Logger logger = LoggerFactory.getLogger(MessageFeedWorker.class);
    private Datafeed datafeed;


    public MessageFeedWorker(SymphonyClient symClient, DataFeedListener dataFeedListener) {
        this.symClient = symClient;
        this.dataFeedListener = dataFeedListener;


    }

    public void run() {


        //noinspection InfiniteLoopStatement
        while (true) {


            try {

                if (datafeed == null) {
                    try {
                        logger.info("Creating datafeed with pod...");

                        datafeed = symClient.getDataFeedClient().createDatafeed();

                    } catch (DataFeedException e) {

                        logger.error("Failed to create datafeed with pod, please check connection..", e);
                        datafeed = null;
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e1) {
                            logger.error("Interrupt.. ", e1);
                        }
                        continue;
                    }

                }


                List<V2BaseMessage> messageList = symClient.getDataFeedClient().getMessagesFromDatafeed(datafeed);

                if(messageList != null) {

                    logger.debug("Received {} messages..", messageList.size());

                    for (V2BaseMessage message : messageList) {
                   //     logger.debug("SymMessage received from stream {} {}", message.getId(),message.getStream());
                        dataFeedListener.onMessage(message);
                    }
                }

            } catch (DataFeedException e) {
                logger.error("Failed to create read datafeed from pod, please check connection..resetting.", e);
                datafeed = null;

            }


        }

    }

}



