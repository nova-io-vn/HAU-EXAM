import test from 'node:test'
import assert from 'node:assert/strict'
import {clampKutePosition,exceededDragThreshold} from './kuteDrag.js'

test('Kute distinguishes click from drag using movement threshold',()=>{assert.equal(exceededDragThreshold(3,4),false);assert.equal(exceededDragThreshold(6,4),true)})
test('Kute position stays inside desktop and mobile viewport safe areas',()=>{assert.deepEqual(clampKutePosition({x:-50,y:999},{width:390,height:844}),{x:10,y:670});assert.deepEqual(clampKutePosition({x:2000,y:-2},{width:1366,height:768}),{x:1278,y:70})})
