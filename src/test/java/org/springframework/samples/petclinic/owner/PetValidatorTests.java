/*
 * Copyright 2012-2024 the original author or authors.
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

package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link PetValidator}
 *
 * @author Wick Dynex
 */
@ExtendWith(MockitoExtension.class)
@DisabledInNativeImage
// Removed 'public' from class declaration to follow SonarQube recommendation.
class PetValidatorTests { // Package-private visibility

	private PetValidator petValidator;

	private Pet pet;

	private PetType petType;

	private Errors errors;

	private static final String petName = "Buddy";

	private static final String petTypeName = "Dog";

	private static final LocalDate petBirthDate = LocalDate.of(1990, 1, 1);

	@BeforeEach
	void setUp() {
		// Initialize objects before each test
		petValidator = new PetValidator();
		pet = new Pet();
		petType = new PetType();
		errors = new MapBindingResult(new HashMap<>(), "pet");
	}

	@Test
	void testValidate() {
		// Setup valid pet data for validation
		petType.setName(petTypeName);
		pet.setName(petName);
		pet.setType(petType);
		pet.setBirthDate(petBirthDate);

		// Validate and check for errors
		petValidator.validate(pet, errors);
		assertFalse(errors.hasErrors()); // No errors expected
	}

	@Nested
	class ValidateHasErrors {

		@Test
		void testValidateWithInvalidPetName() {
			// Setup pet with invalid name (empty string)
			petType.setName(petTypeName);
			pet.setName("");
			pet.setType(petType);
			pet.setBirthDate(petBirthDate);

			// Validate and check for name errors
			petValidator.validate(pet, errors);
			assertTrue(errors.hasFieldErrors("name"));
		}

		@Test
		void testValidateWithInvalidPetType() {
			// Setup pet with null type
			pet.setName(petName);
			pet.setType(null);
			pet.setBirthDate(petBirthDate);

			// Validate and check for type errors
			petValidator.validate(pet, errors);
			assertTrue(errors.hasFieldErrors("type"));
		}

		@Test
		void testValidateWithInvalidBirthDate() {
			// Setup pet with null birthdate
			petType.setName(petTypeName);
			pet.setName(petName);
			pet.setType(petType);
			pet.setBirthDate(null);

			// Validate and check for birthdate errors
			petValidator.validate(pet, errors);
			assertTrue(errors.hasFieldErrors("birthDate"));
		}

	}

}
