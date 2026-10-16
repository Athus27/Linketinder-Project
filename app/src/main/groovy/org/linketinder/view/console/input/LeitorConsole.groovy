package org.linketinder.view.console.input

class LeitorConsole {
    private final Scanner scanner

    LeitorConsole(Scanner scanner) {
        this.scanner = scanner
    }
    String lerLinha(){
        return this.scanner.nextLine()
    }
}
