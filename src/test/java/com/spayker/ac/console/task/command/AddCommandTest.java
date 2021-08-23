package com.spayker.ac.console.task.command;

import org.junit.jupiter.api.BeforeEach;

import static com.spayker.ac.console.task.command.AddCommand.CreateAddCommand;

public class AddCommandTest {

    private AddCommand addCommand;

    @BeforeEach
    public void setup() {

        addCommand = CreateAddCommand();

    }



}
