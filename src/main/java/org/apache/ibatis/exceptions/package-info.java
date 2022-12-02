/*
 *    Copyright 2009-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/**
 * Base package for exceptions.
 * <p>
 * MyBatis中众多的异常类并没有放在exceptions包中。这涉及项目规划时分包问题。通常在规划一个项目的包结构时，可以按照以下两种方式进行包的划分。<br />
 * <li> 按照类型方式划分，例如将所有的接口类放入一个包，将所有的Controller类放入一个包。这种分类方式从类型上看更为清晰，但是会将完成同一功能的多个类分散在不同的包中，不便于模块化开发。</li>
 * <li> 按照功能方式划分，例如将所有与加/解密有关的类放入一个包。在这种分类方式下，同一功能的类内聚性高，便于模块化开发，但会导致同一包内类的类型混乱。</li>
 * 
 * 在项目设计和开发中，推荐优先将功能耦合度高的类放入按照功能划分的包中，而将功能耦合度低或供多个功能使用的类放入按照类型划分的包中。
 */
package org.apache.ibatis.exceptions;
