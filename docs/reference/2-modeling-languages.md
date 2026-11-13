# 2 Modeling languages

karpfen has three textual languages. KMeta describes the types of a domain: what a lamp, a bulb and a switch are, which
values they carry and how they belong together. KModel describes objects of those types and their values. KStates
describes a state machine. A state machine is attached to a type and runs on one object of that type, where it reads and
changes the model.

| File | Language | Content |
|---|---|---|
| `.kmeta` | [2.1 KMeta](2.1-kmeta.md) | A metamodel: the types of a domain with their properties and relations. |
| `.kmodel` | [2.2 KModel](2.2-kmodel.md) | A model: one root object with the objects it holds, all of types of one metamodel. |
| `.kstates` | [2.3 KStates](2.3-kstates.md) | One state machine, attached to a type of the metamodel. |

A project has one domain metamodel, one model and any number of state machines. A model is read against its
metamodel: every object is of a type the metamodel defines, and every value fits a property of that type. A state
machine names its type after `ATTACHED TO`. Which object of the model it runs on is decided when the project runs, by
the object's key, see
[3.3 Running the runtime](https://github.com/karpfenproject/karpfen-runtime/blob/main/docs/reference/3.3-running-the-runtime.md)
and [4.5 Running](https://github.com/karpfenproject/karpfen-IDE/blob/main/docs/reference/4.5-running.md).

A project may have a second `.kmeta` file, the event metamodel. Each of its types is one event, and the properties of
the type are the payload the event carries, see [2.1.7 Event definitions](2.1-kmeta.md#217-event-definitions). A state
machine waits for an event by its domain and its name, and reads the payload of the event that let it change state.

The library checks each text on its own while it reads it, and it checks the state machines of a project against the
metamodels and the model. [2.4 Checks](2.4-checks.md) lists every problem it reports.
[2.5 Using the library](2.5-library.md) shows how to read, check and write the texts from Kotlin code.

The three languages share their lexical rules. Spaces, tabs and line breaks between the parts of a text are free, so the
layout of a text is up to the writer. A comment starts with `//` and runs to the end of the line. It may stand wherever
a space may stand, also at the end of a line. There is no other kind of comment. Keywords are case sensitive. Names,
keys and values are string literals between quotes, and so are numbers and booleans. The only numbers outside quotes are
the tick counts of KStates.

| Rule | KMeta | KModel | KStates |
|---|---|---|---|
| Quotes of a string | `"` | `"` or `'` | `"`, and `'` only inside `EVAL` code |
| Spelling of the keywords | lower case | lower case | upper case; the action operations and `WITH` and `AS` also lower case; `reference` only lower case |
| Quote characters the reader removes from the edges of a value | every `"` | every `"` and `'` | one pair of `'` directly inside the `"` |

A string ends on its line: it cannot hold a line break. Inside a string, a backslash and the character after it belong
to the string, so `\"` does not end it. The value keeps both characters as written: `"say \"hi\""` is not turned into
`say "hi"`. Since the readers remove quote characters at the edges of a value, a value should not start or end with a
quote character. In KStates, `"'on'"` and `"on"` are the same value.

The lamp is the example of this chapter. A room holds a lamp and a wall switch. The lamp holds a bulb and knows the
switch it is wired to. The metamodel has four types:

```kmeta
// The types of the lamp example.
type "Bulb" "A light bulb that burns with watts, up to maxWatts" {
    prop("maxWatts", "number")
    prop("watts", "number")
    prop("lit", "boolean")
}
type "Switch" "A wall switch" {
    prop("position", "string")
    prop("presses", list("string"))
}
type "Lamp" "A lamp with a bulb, wired to a switch" {
    prop("mode", "string")
    has("bulb", "Bulb")
    knows("control", "Switch")
}
type "Room" "The room the lamp stands in" {
    has("lamp", "Lamp")
    has("switch", "Switch")
}
```

The model is a living room. Every object has a key, such as `"lamp"`, by which a `knows` relation and a run refer to it:

```kmodel
make object "living room":"Room" {
    has("lamp") -> make object "lamp":"Lamp" {
        prop("mode") -> "off"
        has("bulb") -> make object "bulb":"Bulb" {
            prop("maxWatts") -> "40"
            prop("watts") -> "0"
            prop("lit") -> "false"
        }
        knows("control") -> "wallSwitch"
    }
    has("switch") -> make object "wallSwitch":"Switch" {
        prop("position") -> "down"
    }
}
```

The state machine of the lamp runs on the object `"lamp"`. It starts in the state `off`. An event `press` in the domain
`switch` turns the lamp on, and the next one turns it off again. On entering a state, the state machine writes the mode and
the values of the bulb:

```kstates
STATEMACHINE ATTACHED TO "Lamp" {
    STATES {
        INITIAL STATE "off" {
            ENTRY {
                SET("mode", "off")
                SET("bulb->lit", "false")
                SET("bulb->watts", "0")
            }
        }
        STATE "on" {
            ENTRY {
                SET("mode", "on")
                SET("bulb->lit", "true")
                SET("bulb->watts", EVAL { return $(bulb->maxWatts) })
            }
        }
    }
    TRANSITIONS {
        TRANSITION "off" -> "on" {
            CONDITION { EVENT("switch", "press") }
        }
        TRANSITION "on" -> "off" {
            CONDITION { EVENT("switch", "press") }
        }
    }
}
```

The paths `"mode"` and `"bulb->watts"` start at the object the state machine runs on, the lamp. `$(bulb->maxWatts)` in
the `EVAL` code is the current value of that property, 40 here. The sections of this chapter extend these three texts.
The full lamp project is the walkthrough of the IDE, see
[1 Getting started](https://github.com/karpfenproject/karpfen-IDE/blob/main/docs/reference/1-getting-started.md).
