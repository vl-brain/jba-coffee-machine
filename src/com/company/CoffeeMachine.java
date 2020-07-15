package com.company;

import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CoffeeMachine {
    private int money;
    private int water;
    private int milk;
    private int coffeeBeans;
    private int cups;
    private InternalState currentState = InternalState.OFF;

    private enum Coffee {
        ESPRESSO(250, 0, 16, 4),
        LATTE(350, 75, 20, 7),
        CAPPUCCINO(200, 100, 12, 6);
        private final int water;
        private final int milk;
        private final int coffeeBeans;
        private final int money;

        Coffee(int water, int milk, int coffeeBeans, int money) {
            this.water = water;
            this.milk = milk;
            this.coffeeBeans = coffeeBeans;
            this.money = money;
        }
    }

    private enum Command {
        START(InternalState.OFF) {
            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                return InternalState.WAIT_COMMAND;
            }
        },
        BUY(InternalState.WAIT_COMMAND) {
            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                return InternalState.WAIT_BUY_COFFEE;
            }
        },
        FILL(InternalState.WAIT_COMMAND) {
            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                return InternalState.WAIT_FILL_WATER;
            }
        },
        TAKE(InternalState.WAIT_COMMAND) {
            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                coffeeMachine.giveMoney();
                return getState();
            }
        },
        REMAINING(InternalState.WAIT_COMMAND) {
            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                coffeeMachine.showState();
                return getState();
            }
        },
        EXIT(InternalState.WAIT_COMMAND) {
            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                return InternalState.OFF;
            }
        },
        SELECT_COFFEE(InternalState.WAIT_BUY_COFFEE) {
            private Coffee coffee;

            @Override
            protected boolean acceptCommand(String command) {
                int option = command.matches("\\d+") ? Integer.parseInt(command) : 0;
                if (0 < option && option <= Coffee.values().length) {
                    coffee = Coffee.values()[option - 1];
                    return true;
                }
                return false;
            }

            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                coffeeMachine.sellCoffee(coffee);
                return InternalState.WAIT_COMMAND;
            }

            @Override
            public String getDescription() {
                return Arrays.stream(Coffee.values())
                        .map(value -> (value.ordinal() + 1) + " - " + value.name().toLowerCase())
                        .collect(Collectors.joining(", "));
            }
        },
        BACK(InternalState.WAIT_BUY_COFFEE) {
            @Override
            public String getDescription() {
                return super.getDescription() + " - to main menu";
            }

            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                return InternalState.WAIT_COMMAND;
            }
        },
        FILL_WATER(InternalState.WAIT_FILL_WATER) {
            private int water;

            @Override
            protected boolean acceptCommand(String command) {
                boolean matches = command.matches("\\d+");
                water = matches ? Integer.parseInt(command) : 0;
                return matches;
            }

            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                coffeeMachine.fillWater(water);
                return InternalState.WAIT_FILL_MILK;
            }
        },
        FILL_MILK(InternalState.WAIT_FILL_MILK) {
            private int milk;

            @Override
            protected boolean acceptCommand(String command) {
                boolean matches = command.matches("\\d+");
                milk = matches ? Integer.parseInt(command) : 0;
                return matches;
            }

            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                coffeeMachine.fillMilk(milk);
                return InternalState.WAIT_FILL_COFFEE_BEANS;
            }
        },
        FILL_COFFEE_BEANS(InternalState.WAIT_FILL_COFFEE_BEANS) {
            private int coffeeBeans;

            @Override
            protected boolean acceptCommand(String command) {
                boolean matches = command.matches("\\d+");
                coffeeBeans = matches ? Integer.parseInt(command) : 0;
                return matches;
            }

            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                coffeeMachine.fillCoffeeBeans(coffeeBeans);
                return InternalState.WAIT_FILL_CUPS;
            }
        },
        FILL_CUPS(InternalState.WAIT_FILL_CUPS) {
            private int cups;

            @Override
            protected boolean acceptCommand(String command) {
                boolean matches = command.matches("\\d+");
                cups = matches ? Integer.parseInt(command) : 0;
                return matches;
            }

            @Override
            public InternalState execute(CoffeeMachine coffeeMachine) {
                coffeeMachine.fillCups(cups);
                return InternalState.WAIT_COMMAND;
            }
        };
        private final InternalState state;

        Command(InternalState state) {
            this.state = state;
        }

        protected InternalState getState() {
            return state;
        }

        protected boolean acceptCommand(String command) {
            return name().equals(command);
        }

        public static Optional<Command> findCommand(InternalState state, String command) {
            return Arrays.stream(values())
                    .filter(action -> action.state == state && action.acceptCommand(command))
                    .findFirst();
        }

        public static Command[] valuesOnState(InternalState state) {
            return Arrays.stream(values())
                    .filter(value -> value.state == state)
                    .toArray(Command[]::new);
        }

        public abstract InternalState execute(CoffeeMachine coffeeMachine);

        public String getDescription() {
            return name().toLowerCase();
        }
    }


    private enum InternalState {
        OFF {
            @Override
            public String getTitle() {
                return "";
            }
        },
        WAIT_COMMAND {
            @Override
            public String getTitle() {
                return Arrays.stream(Command.valuesOnState(this))
                        .map(Command::getDescription)
                        .collect(Collectors.joining(", ", "Write action (", "):"));
            }
        },
        WAIT_BUY_COFFEE {
            @Override
            public String getTitle() {
                return Arrays.stream(Command.valuesOnState(this))
                        .map(Command::getDescription)
                        .collect(Collectors.joining(", ", "What do you want to buy? ", ":"));
            }
        },
        WAIT_FILL_WATER {
            @Override
            public String getTitle() {
                return "Write how many ml of water do you want to add:";
            }
        },
        WAIT_FILL_MILK {
            @Override
            public String getTitle() {
                return "Write how many ml of milk do you want to add:";
            }
        },
        WAIT_FILL_COFFEE_BEANS {
            @Override
            public String getTitle() {
                return "Write how many grams of coffee beans do you want to add:";
            }
        },
        WAIT_FILL_CUPS {
            @Override
            public String getTitle() {
                return "Write how many disposable cups of coffee do you want to add:";
            }
        };

        public abstract String getTitle();
    }

    public CoffeeMachine(int money, int water, int milk, int coffeeBeans, int cups) {
        this.money = money;
        this.water = water;
        this.milk = milk;
        this.coffeeBeans = coffeeBeans;
        this.cups = cups;
    }

    public void run() {
        execute(Command.START.name());
    }

    public boolean isRunning() {
        return currentState != InternalState.OFF;
    }

    public void execute(String command) {
        Command.findCommand(currentState, command.toUpperCase())
                .ifPresent(cmd -> currentState = cmd.execute(this));
        showMessage(currentState.getTitle());
    }

    private void showMessage(String message) {
        if (!message.isEmpty()) {
            System.out.println(message);
        }
    }

    private void sellCoffee(Coffee coffee) {
        int remainWater = water - coffee.water;
        int remainMilk = milk - coffee.milk;
        int remainCoffeeBeans = coffeeBeans - coffee.coffeeBeans;
        int remainCups = cups - 1;
        if (remainWater < 0) {
            showMessage("Sorry, not enough water!");
        } else if (remainMilk < 0) {
            showMessage("Sorry, not enough milk!");
        } else if (remainCoffeeBeans < 0) {
            showMessage("Sorry, not enough coffee beans!");
        } else if (remainCups < 0) {
            showMessage("Sorry, not enough disposable cups!");
        } else {
            showMessage("I have enough resources, making you a coffee!");
            water = remainWater;
            milk = remainMilk;
            coffeeBeans = remainCoffeeBeans;
            cups = remainCups;
            money += coffee.money;
        }
    }

    private void fillWater(int water) {
        this.water += water;
    }

    private void fillMilk(int milk) {
        this.milk += milk;
    }

    private void fillCoffeeBeans(int coffeeBeans) {
        this.coffeeBeans += coffeeBeans;
    }

    private void fillCups(int cups) {
        this.cups += cups;
    }

    private void giveMoney() {
        showMessage("I gave you $" + money);
        money = 0;
    }

    private void showState() {
        showMessage("The coffee machine has:\n" +
                water + " of water\n" +
                milk + " of milk\n" +
                coffeeBeans + " of coffee beans\n" +
                cups + " of disposable cups\n" +
                money + " of money");
    }

    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);
        final CoffeeMachine coffeeMachine = new CoffeeMachine(550, 400, 540, 120, 9);
        coffeeMachine.run();
        while (coffeeMachine.isRunning()) {
            coffeeMachine.execute(scanner.next());
        }
    }
}
