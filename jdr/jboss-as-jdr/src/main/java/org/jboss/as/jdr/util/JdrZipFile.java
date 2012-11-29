/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.jdr.util;

import org.jboss.as.jdr.commands.JdrEnvironment;
import org.jboss.vfs.VirtualFile;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import static org.jboss.as.jdr.JdrLogger.ROOT_LOGGER;

public class JdrZipFile {

    ZipOutputStream zos;
    String jbossHome;
    JdrEnvironment env;
    String name;
    String baseName;

    public JdrZipFile(JdrEnvironment env) throws Exception {
        this.env = env;
        this.jbossHome = this.env.getJbossHome();
        SimpleDateFormat fmt = new SimpleDateFormat("yy-MM-dd_hh-mm-ss");
        baseName = "jdr_" + fmt.format(new Date());
        this.name = this.env.getOutputDirectory() +
                    java.io.File.separator +
                    baseName + ".zip";

        if (this.env.getHostControllerName() != null) {
            this.name += "." + this.env.getHostControllerName();
        }

        if (this.env.getServerName() != null) {
            this.name += "_" + this.env.getServerName();
        }

        zos = new ZipOutputStream(new FileOutputStream(this.name));
    }

    public String name() {
        return this.name;
    }

    public void add(InputStream is, String path) {
        byte [] buffer = new byte[1024];

        try {
            String entryName = this.baseName + "/" + path;
            ZipEntry ze = new ZipEntry(entryName);
            zos.putNextEntry(ze);
            int bytesRead = is.read(buffer);
            while( bytesRead > -1 ) {
                zos.write(buffer, 0, bytesRead);
                bytesRead = is.read(buffer);
            }
        }
        catch (ZipException ze) {
            ROOT_LOGGER.debugf(ze, "%s is already in the zip", path);
        }
        catch (Exception e) {
            ROOT_LOGGER.debugf(e, "Error when adding %s", path);
        }
        finally {
            try {
                zos.closeEntry();
            }
            catch (Exception e) {
                ROOT_LOGGER.debugf(e, "Error when closing entry for %s", path);
            }
        }
    }

    public void add(VirtualFile file, InputStream is) throws Exception {
        String name = "JBOSS_HOME" + file.getPathName().substring(this.jbossHome.length());
        this.add(is, name);
    }

    public void add(String content, String path) throws Exception {
        String name = "sos_strings/as7/" + path;
        this.add(new ByteArrayInputStream(content.getBytes()), name);
    }

    public void addLog(String content, String logName) throws Exception {
        String name = "sos_logs/" + logName;
        this.add(new ByteArrayInputStream(content.getBytes()), name);
    }

    public void close() throws Exception {
        this.zos.close();
    }
}
