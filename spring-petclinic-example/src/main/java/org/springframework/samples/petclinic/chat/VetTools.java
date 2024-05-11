/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.chat;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.vet.Vets;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author Zhiyong Li
 */
@Component
public class VetTools {

	private final VetRepository vetRepository;

	public VetTools(VetRepository vetRepository) {
		this.vetRepository = vetRepository;
	}

	@Tool(value = { "return list of Vets, include their specialist" })
	public Collection<Vet> getVetList() {
		return vetRepository.findAll();
	}

}
