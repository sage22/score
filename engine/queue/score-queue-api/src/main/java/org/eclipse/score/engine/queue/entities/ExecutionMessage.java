/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
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
package org.eclipse.score.engine.queue.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.eclipse.score.engine.node.entities.WorkerNode;
import org.eclipse.score.facade.entities.Execution;
import org.eclipse.score.orchestrator.entities.Message;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * User:
 * Date: 10/09/12
 * Time: 11:11
 */
public class ExecutionMessage implements Message, Cloneable {

    private static final long serialVersionUID = 3523623124812765964L;

	public static final long EMPTY_EXEC_STATE_ID = -1L;
	public static final String EMPTY_WORKER = "EMPTY";

	private long execStateId;
	private String workerId;
	private String workerGroup;
	private ExecStatus status;
	private Payload payload;
	private int msgSeqId;
	private String msgId;
    private Date createDate;

	private transient String workerKey;

    private transient Execution executionObject;

	public ExecutionMessage() {
		execStateId = EMPTY_EXEC_STATE_ID;
		workerId = ExecutionMessage.EMPTY_WORKER;
		workerGroup = "";
		status = ExecStatus.INIT;
		payload = null;
		msgSeqId = -1;
		msgId = "";
        createDate = null;
	}

    public ExecutionMessage(String executionId, Payload payload) {
        this.execStateId = ExecutionMessage.EMPTY_EXEC_STATE_ID;
        this.workerId = ExecutionMessage.EMPTY_WORKER;
        this.workerGroup = WorkerNode.DEFAULT_WORKER_GROUPS[0];
        this.msgId = String.valueOf(executionId);
        this.status = ExecStatus.PENDING;
        this.payload = payload;
        this.msgSeqId = 0;
    }

    public ExecutionMessage(long execStateId,
    	                        String workerId,
    	                        String workerGroup,
    	                        String msgId,
    	                        ExecStatus status,
    	                        Payload payload,
    	                        int msgSeqId,
                                Date createDate) {
    		this.execStateId = execStateId;
    		this.workerId = workerId;
    		this.workerGroup = workerGroup;
    		this.msgId = msgId;
    		this.status = status;
    		this.payload = payload;
    		this.msgSeqId = msgSeqId;
            this.createDate = createDate;
   }

	public ExecutionMessage(long execStateId,
	                        String workerId,
	                        String workerGroup,
	                        String msgId,
	                        ExecStatus status,
	                        Payload payload,
	                        int msgSeqId) {
		this.execStateId = execStateId;
		this.workerId = workerId;
		this.workerGroup = workerGroup;
		this.msgId = msgId;
		this.status = status;
		this.payload = payload;
		this.msgSeqId = msgSeqId;
	}

    public ExecutionMessage(long execStateId,
                            String workerId,
                            String workerGroup,
                            String msgId,
                            ExecStatus status,
                            Execution executionObject,
                            Payload payload,
                            int msgSeqId) {
        this.execStateId = execStateId;
        this.workerId = workerId;
        this.workerGroup = workerGroup;
        this.msgId = msgId;
        this.status = status;
        this.executionObject = executionObject;
        this.payload = payload;
        this.msgSeqId = msgSeqId;
    }

    public Execution getExecutionObject() {
        return executionObject;
    }

    public void setExecutionObject(Execution executionObject) {
        this.executionObject = executionObject;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

	public long getExecStateId() {
		return execStateId;
	}

	public void setExecStateId(long id) {
		this.execStateId = id;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msg_id) {
		this.msgId = msg_id;
	}

	public String getWorkerId() {
		return workerId;
	}

	public String getWorkerGroup() {
		return workerGroup;
	}

	@JsonIgnore
	public String getMsgUniqueId() {
		return msgId + ":" + msgSeqId;
	}

	public ExecStatus getStatus() {
		return status;
	}

	public Payload getPayload() {
		return payload;
	}

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public int getMsgSeqId() {
		return msgSeqId;
	}

	public void setStatus(ExecStatus status) {
		this.status = status;
	}

	synchronized public void incMsgSeqId() {
		this.msgSeqId = msgSeqId + 1;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	@Override
	public int getWeight() {
		return 1;
	}

	@Override
	public String getId() {
		return workerKey;
	}

	public String getWorkerKey() {
		return workerKey;
	}

	public ExecutionMessage setWorkerKey(String workerKey) {
		this.workerKey = workerKey;
		return this;
	}

	@Override
	public List<Message> shrink(List<Message> messages) {
        if (messages.size() > 2) {
            ExecutionMessage firstMessage  = (ExecutionMessage) messages.get(0);
            ExecutionMessage secondMessage = (ExecutionMessage) messages.get(1);
            ExecutionMessage lastMessage = (ExecutionMessage) messages.get(messages.size()-1);

            if (firstMessage.getStatus().equals(ExecStatus.IN_PROGRESS)) {
//                if(logger.isDebugEnabled())
//                    logger.debug("Shrinking... Keeping second and last from messages: \n" + messagesToString(messages));
				return Arrays.asList((Message)secondMessage, lastMessage);
			}
            else {
//                if(logger.isDebugEnabled())
//                    logger.debug("Shrinking... Keeping first and last from messages: \n" + messagesToString(messages));
				return Arrays.asList((Message)firstMessage, lastMessage);
			}
		} else {
			return messages;
		}
	}

    private String messagesToString(List<Message> messages){
        StringBuilder str = new StringBuilder();

        for(Message m : messages){
            str.append(m.toString()).append("\n");
        }

       return str.toString();
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();

        boolean isAck = this.getExecutionObject() == null;

        str.append(" ExecutionId:").append(this.msgId).
        append(" ExecStateId:").append(this.execStateId).
        append(" Status:").append(this.status).
        append(" WorkerKey:").append(this.getId()).
                append(" IsAck:").append(isAck);

        return str.toString();
    }

	@SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
	@Override
	public Object clone() {
		try {
			ExecutionMessage cloned = (ExecutionMessage) super.clone();
			if (payload != null) cloned.payload = (Payload) (payload.clone());
			return cloned;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException("Failed to clone message", ex);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ExecutionMessage that = (ExecutionMessage) o;
		return new EqualsBuilder()
				.append(this.execStateId, that.execStateId)
				.append(this.msgSeqId, that.msgSeqId)
				.append(this.msgId, that.msgId)
				.append(this.payload, that.payload)
				.append(this.status, that.status)
				.append(this.workerGroup, that.workerGroup)
				.append(this.workerId, that.workerId)
                .append(this.createDate, that.createDate)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				workerId,
				workerGroup,
				msgId,
				status,
				payload,
				msgSeqId,
				execStateId,
                createDate
		);
	}
}