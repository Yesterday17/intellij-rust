enum E {
    pub F // Enum variants are always implicitly public, and `pub` keyword is forbidden here
}

fn foo() {} // Must be parsed correctly
