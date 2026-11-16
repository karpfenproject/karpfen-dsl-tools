# 2 Modeling languages

karpfen has three textual languages. KMeta describes the types of a domain: what a lamp, a bulb and a switch are, which
values they carry and how they belong together. KModel describes the objects of those types and gives them their
values. KStates describes a state machine. A state machine is attached to a type and runs on one object of that type,
and it reads and changes the model starting from that object.

| File | Language | Content |
|---|---|---|
| `.kmeta` | [2.1 KMeta](2.1-kmeta.md) | A metamodel: the types of a domain with their properties and relations. |
| `.kmodel` | [2.2 KModel](2.2-kmodel.md) | A model: one root object with the objects it holds, all of types of one metamodel. |
| `.kstates` | [2.3 KStates](2.3-kstates.md) | One state machine, attached to a type of the metamodel or attachable to several. |

A project has one domain metamodel, one model and any number of state machines. A model is read against its
metamodel. Every object must be of a type that the metamodel defines, and every value must fit a property of that type.

A state machine names its type after `ATTACHED TO`. The object of the model it runs on is not written in the state
machine. It is chosen by its key when the project runs, as described for the runtime in
[3.3 Running the runtime](https://github.com/karpfenproject/karpfen-runtime/blob/main/docs/reference/3.3-running-the-runtime.md)
and for the IDE in [4.5 Running](https://github.com/karpfenproject/karpfen-IDE/blob/main/docs/reference/4.5-running.md).
An attachable state machine is not attached to an object by a key. It has a name, and objects get instances of it in
the two ways of
[2.3.11 State machine management and lifecycle](2.3-kstates.md#2311-state-machine-management-and-lifecycle): through
their types and through the actions of other state machines.

A project may have a second `.kmeta` file, the event metamodel, which is written as
[2.1.7 Event definitions](2.1-kmeta.md#217-event-definitions) shows. Each of its types defines one event, and the
properties of the type are the payload that the event carries. A state machine waits for an event by its domain and its
name. When an event lets the state machine change state, the state machine can read the payload of that event.

The library checks each text on its own while it reads it. It also checks the state machines of a project against the
metamodels and the model. Every problem it reports is listed with its code in [2.4 Checks](2.4-checks.md). Kotlin
programs read, check and write the texts with the library that [2.5 Using the library](2.5-library.md) introduces.

The three languages follow the same lexical rules, apart from the differences in the table below. Spaces, tabs and line
breaks between the parts of a text carry no meaning, and the writer is free to lay out a text as they like. A comment
starts with `//` and runs to the end of the line. It may stand wherever a space may stand, also at the end of a line,
and there is no other kind of comment. Keywords are case sensitive. Names, keys and values are string literals between
quotes. This holds for numbers and booleans as well, and the only numbers written without quotes are the tick counts of
KStates.

| Rule | KMeta | KModel | KStates |
|---|---|---|---|
| Quotes of a string | `"` | `"` or `'` | `"`, and `'` only inside `EVAL` code |
| Spelling of the keywords | lower case | lower case | upper case, some also in lower case, `reference` only in lower case |
| Quote characters the reader removes from the edges of a value | every `"` | every `"` and `'` | one pair of `'` directly inside the `"` |

In KStates the operations `SET`, `SETLIST`, `DROPLIST`, `APPEND`, `SETOBJ`, `APPENDOBJ`, `DROPOBJ` and `EXCHANGE`
and the keywords `ATTACH`, `WITH` and `AS` may also be written in lower case, such as `set` or `attach`. `EVENT` has
only its upper case form, as an operation and as a clause of a condition. `reference` has only its lower case form.
Every other keyword of KStates is written in upper case only.

Inside a string a backslash and the character after it belong together. Because of this rule `\"` does not end a string,
and a line break directly after a backslash stays inside it. A line break without a backslash in front cannot stand in a
string. The value keeps both characters of such a pair as written: `"say \"hi\""` is not turned into `say "hi"`, and an
escaped line break leaves the backslash and the line break in the value. Since the readers remove quote characters at
the edges of a value, a value should not start or end with a quote character. In KStates, `"'on'"` and `"on"` are the
same value.

The examples of this chapter are about a lamp in a living room. The room holds the lamp and a wall switch, and the lamp
holds a bulb and knows the switch it is wired to. The metamodel has four types.

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

The model is the living room. Every object has a key, such as `"lamp"`. A `knows` relation refers to an object by its
key, and a run uses the key in the same way to name the object that a state machine runs on.

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

The state machine of the lamp runs on the object `"lamp"` and starts in the state `off`. An event `press` in the
domain `switch` turns the lamp on, and the next `press` turns it off again. Whenever the state machine enters a state,
it writes the mode of the lamp and the values of the bulb.

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

The paths `"mode"` and `"bulb->watts"` start at the lamp, because the lamp is the object the state machine runs on. In
the `EVAL` code, `$(bulb->maxWatts)` stands for the current value of that property. In this model it is 40.

The sections of this chapter extend these three texts. The complete lamp example project is the walkthrough of the IDE
in [1 Getting started](https://github.com/karpfenproject/karpfen-IDE/blob/main/docs/reference/1-getting-started.md).
