/*
 * Copyright 2017 by MediData AG
 * ALL RIGHTS RESERVED
 *
 * MediData AG - CommercialSoftwareLicense
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * $Id$
 *
 * LogProdId:  nn
 * LogClassId: nnnn
 * This file is part of: md-test-paxsupport, md-test-servermock
 */
package io.devcon5.commons.rest;

import static java.lang.Integer.getInteger;
import static java.lang.Math.min;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.stream.IntStream;

public final class Ports {

   private static final int MAX_RETRIES = min(getInteger("portsearch.retry.limit", 8192), 65536 - 1024); //max-ports - reserved-ports

   private Ports() {
   }

   /**
    * Retrives a single available port
    *
    * @param excludes list of ports that should be ignored
    * @return a single port number
    */
   public static int findAvailablePort(int... excludes) {
      return findAvailablePortRange(null, 1, excludes)[0];
   }


   /**
    * Returns an sequence of available, consecutive ports.
    *
    * @param addr     a specific inet address to search for available/unbound ports
    * @param numPorts the number of ports to retrieve
    * @param excludes list of port to exclude. None of these ports are allowed in the range
    *
    * @return
    */
   public static int[] findAvailablePortRange(final InetAddress addr, int numPorts, int... excludes) {
      //this limit is rather arbitrary but should prevent unforseen endless looping here
      int maxRetries = MAX_RETRIES;
      while (maxRetries-- > 0) {
         try (ServerSocket socket = newSocket(addr, 0)) {
            final int port = socket.getLocalPort();
            if (isExcluded(port, excludes) || !nextPortsAvailable(addr, port, numPorts, excludes)) {
               continue;
            }
            return IntStream.range(port, port + numPorts).toArray();
         } catch (Exception e) {
            throw new IllegalStateException("Unable to find available port", e);
         }
      }
      throw new IllegalStateException("Unable to find portrange within retry limit " + MAX_RETRIES);
   }

   private static boolean nextPortsAvailable(final InetAddress addr, final int port, final int numPorts, final int[] excludes) {
      return IntStream.of(port + 1, port + numPorts).noneMatch(nextPort -> isExcluded(nextPort, excludes) || !isAvailable(addr, nextPort));
   }

   private static boolean isExcluded(final int port, int... excludes) {
      return IntStream.of(excludes).anyMatch(ex -> ex == port);
   }

   /**
    * Checks if the specified port is available
    *
    * @param port the port to verify
    *
    * @return <code>true</code> if the port is available
    */
   public static boolean isAvailable(final InetAddress addr, int port) {
      try (ServerSocket socket = newSocket(addr, port)) {
      } catch (Exception ignored) {
         return false;
      }
      return true;
   }

   private static ServerSocket newSocket(final InetAddress addr, final int port) throws IOException {
      final ServerSocket socket = new ServerSocket();
      //the the reuseAddress flag is important so that the checked port can be reused after it has been bound
      //we need to bind the port in order to find out it is available
      socket.setReuseAddress(true);
      if (addr == null) {
         socket.bind(new InetSocketAddress(port));
      } else {
         socket.bind(new InetSocketAddress(addr, port));
      }
      return socket;
   }

}
