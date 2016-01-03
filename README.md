spreadsheet
===========

A work in progress single page Google Sheets-style spreadsheet clone in ClojureScript, Reagent, and re-frame.

Current Controls
----------------

* Double click to edit a cell
* Enter in edit mode to save a cell
* Clicking outside of sheet will save a cell
* Clicking other cells in edit mode will add the cell's row and column to the formula
* Right-clicking two cells in the same row or column is equivalent to "dragging down"
  the first cell into the second cell (so "A1 + B1" will drag down to "A2 + B2", "A3 + B3", etc.)

Playing around in the REPL
--------------------------

To play around in the REPL, run:

```
lein figwheel
```

then go to localhost:3449 on your browser after it finishes loading. You will then have access
to the ClojureScript REPL (you can play with the front-end with this).

To test sending events to the app, you first have to require the re-frame dispatch function.
To do this run this in the ClojureScript REPL:

```clojure
(require '[re-frame.core :refer [dispatch]])
```

Now you are the complete controller of the app! To show off your new powers try to update
a spreadsheet cell through the ClojureScript REPL by running:

```clojure
(dispatch [:update-formula 0 0 "Perfect"])
```

Oh no! The cell in the top left corner has now become Perfect Cell!

A cool thing about lein figwheel is that any changes to your source code is automatically
reloaded in the browser with all of your state changes intact. So no wasted time reloading the
browser and no wasted time reentering test data through the front-end.

TODO:
-----

* Check formulas for correctness before saving to cells
* Determine cells that a cell depends on for calculations based on the formula
* Evaluate formulas for cells
* Implement 'drag down' to copy formulas
