/**
 * Copyright (c) 2012, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.cloudera.crunch.io.impl;

import java.io.IOException;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import com.cloudera.crunch.Source;
import com.cloudera.crunch.impl.mr.run.CrunchInputs;
import com.cloudera.crunch.io.SourceTargetHelper;
import com.cloudera.crunch.type.PType;

public abstract class FileSourceImpl<T> implements Source<T> {

  private static final Log LOG = LogFactory.getLog(FileSourceImpl.class);
  
  protected final Path path;
  protected final PType<T> ptype;
  protected final Class<? extends FileInputFormat> inputFormatClass;
  
  public FileSourceImpl(Path path, PType<T> ptype, Class<? extends FileInputFormat> inputFormatClass) {
	this.path = path;
	this.ptype = ptype;
	this.inputFormatClass = inputFormatClass;
  }
  
  @Override
  public PType<T> getType() {
	return ptype;
  }
  
  @Override
  public void configureSource(Job job, int inputId) throws IOException {
	if (inputId == -1) {
      FileInputFormat.addInputPath(job, path);
      job.setInputFormatClass(inputFormatClass);
    } else {
      CrunchInputs.addInputPath(job, path, inputFormatClass, inputId);
    }
  }

  @Override
  public long getSize(Configuration configuration) {
	try {
	  return SourceTargetHelper.getPathSize(configuration, path);
	} catch (IOException e) {
	  LOG.warn(String.format("Exception thrown looking up size of: %s", path), e);
	}
	return 1L;
  }


  @Override
  public boolean equals(Object other) {
    if (other == null || !getClass().equals(other.getClass())) {
      return false;
    }
    FileSourceImpl o = (FileSourceImpl) other;
    return ptype.equals(o.ptype) && path.equals(o.path) &&
        inputFormatClass.equals(o.inputFormatClass);
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(ptype).append(path)
        .append(inputFormatClass).toHashCode();
  }
  
  @Override
  public String toString() {
	return new StringBuilder().append(inputFormatClass.getSimpleName())
	    .append("(").append(path).append(")").toString();
  }
}
